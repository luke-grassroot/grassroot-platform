package za.org.grassroot.integration.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import za.org.grassroot.core.domain.Account;
import za.org.grassroot.core.domain.AccountBillingRecord;
import za.org.grassroot.core.repository.AccountBillingRecordRepository;
import za.org.grassroot.integration.email.EmailSendingBroker;
import za.org.grassroot.integration.email.GrassrootEmail;
import za.org.grassroot.integration.exception.PaymentMethodFailedException;
import za.org.grassroot.integration.payments.peachp.PaymentErrorPP;
import za.org.grassroot.integration.payments.peachp.PaymentResponsePP;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Objects;

import static za.org.grassroot.core.specifications.AccountSpecifications.*;

/**
 * Created by luke on 2016/10/26.
 */
@Service
@PropertySource(value = "${grassroot.payments.properties}", ignoreResourceNotFound = true)
public class PaymentServiceBrokerImpl implements PaymentServiceBroker {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceBrokerImpl.class);

    private static final String MONTH_FORMAT = "%1$02d";
    private static final String YEAR_FORMAT = "20%d";
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("#.00");

    private static final String PRE_AUTH = "PA";
    private static final String DEBIT = "DB";
    private static final String RECURRING = "PA";

    private static final String INITIAL = "INITIAL";
    private static final String REPEAT = "REPEATED";

    private static final String OKAY_CODE = "000.100.110";
    private static final String NO_3D_CODE = "100.390.109";

    private AccountBillingRecordRepository billingRepository;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private EmailSendingBroker emailSendingBroker;

    private UriComponentsBuilder baseUriBuilder;
    private HttpHeaders stdHeaders;

    @Value("${grassroot.payments.enabled:false}")
    private boolean paymentsEnabled;

    @Value("${grassroot.payments.host:localhost}")
    private String paymentsRestHost;
    @Value("${grassroot.payments.params.auth.user:user}")
    private String paymentsAuthUserIdParam;
    @Value("${grassroot.payments.params.auth.password:pwd}")
    private String paymentsAuthPasswordParam;
    @Value("${grassroot.payments.params.auth.channelId:channel}")
    private String paymentsAuthChannelIdParam;

    @Value("${grassroot.payments.initial.path:/paytest}")
    private String initialPaymentRestPath;
    @Value("${grassroot.payments.recurring.path:/paytset}")
    private String recurringPaymentRestPath; // make a format string so can include registration ID appropriately

    @Value("${grassroot.payments.params.amount:amount}")
    private String paymentAmountParam;
    @Value("${grassroot.payments.params.currency:currency}")
    private String paymentCurrencyParam;
    @Value("${grassroot.payments.params.brand:brand}")
    private String paymentCardBrand;
    @Value("${grassroot.payments.params.type:type}")
    private String paymentTypeParam;
    @Value("${grassroot.payments.params.statementId:transid}")
    private String paymentTransIdParam;

    @Value("${grassroot.payments.params.cardnumber:card}")
    private String cardNumberParam;
    @Value("${grassroot.payments.params.holder:name}")
    private String cardHolderParam;
    @Value("${grassroot.payments.params.expiry.month:month}")
    private String cardExpiryMonthParam;
    @Value("${grassroot.payments.params.expiry.year:year}")
    private String cardExpiryYearParam;
    @Value("${grassroot.payments.params.cvv:seccode}")
    private String securityCodeParam;
    @Value("${grassroot.payments.params.recurring:recurring}")
    private String recurringParam;
    @Value("${grassroot.payments.params.regflag:registered}")
    private String registrationFlag;

    @Value("${grassroot.payments.values.user:grassroot}")
    private String userId;
    @Value("${grassroot.payments.values.password:grasroot}")
    private String password;
    @Value("${grassroot.payments.values.channelId:testChannel}")
    private String channelId;
    @Value("${grassroot.payments.values.channelId3d:testChannel2}")
    private String channelId3d;
    @Value("${grassroot.payments.values.currency:ZAR}")
    private String currency;

    @Value("${grassroot.payments.email.address:payments@grassroot}")
    private String paymentsEmailNotification;

    @Autowired
    public PaymentServiceBrokerImpl(AccountBillingRecordRepository billingRepository,
                                    RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.billingRepository = billingRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Autowired(required = false)
    public void setEmailSendingBroker(EmailSendingBroker emailSendingBroker) {
        this.emailSendingBroker = emailSendingBroker;
    }

    @PostConstruct
    public void init() {
        baseUriBuilder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(paymentsRestHost)
                .queryParam(paymentsAuthUserIdParam, userId)
                .queryParam(paymentsAuthPasswordParam, password)
                .queryParam(paymentCurrencyParam, currency);

        stdHeaders = new HttpHeaders();
        stdHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        stdHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
    }

    @Override
    @Transactional
    public boolean linkPaymentMethodToAccount(PaymentMethod paymentMethod, String accountUid, AccountBillingRecord billingRecord, boolean deleteBillOnFailure) {
        final double amountToPay = (double) billingRecord.getTotalAmountToPay() / 100;
        logger.info("About to charge R{} to payment method, from billing record for {}", amountToPay, billingRecord.getTotalAmountToPay());
        UriComponentsBuilder paymentUri = generateInitialPaymentUri(paymentMethod, amountToPay);
        try {
            HttpEntity<PaymentResponsePP> request = new HttpEntity<>(stdHeaders);
            // logger.info("URL: " + paymentUri.toUriString());
            ResponseEntity<PaymentResponsePP> response = restTemplate.exchange(paymentUri.build().toUri(), HttpMethod.POST, request, PaymentResponsePP.class);
            PaymentResponsePP okayResponse = response.getBody();
            logger.info("Payment Success!: With reference : {}", okayResponse.getRegistrationId());

            Account account = billingRecord.getAccount();
            account.setPaymentRef(okayResponse.getRegistrationId());
            handleSuccessfulPayment(billingRecord, okayResponse);
            return true;
        } catch (HttpStatusCodeException e) {
            try {
                PaymentErrorPP errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), PaymentErrorPP.class);
                logger.info("Payment Error!: {}", errorResponse.toString());
            } catch (IOException error) {
                logger.info("Could not read in JSON!");
                error.printStackTrace();
            }
            return false;
        }
    }

    @Override
    @Transactional
    public PaymentResponse asyncPaymentInitiate(String accountUid, PaymentMethod method, AccountBillingRecord amountToPay, String returnToUrl) {
        Objects.requireNonNull(accountUid);
        Objects.requireNonNull(method);
        Objects.requireNonNull(returnToUrl);

        try {
            UriComponentsBuilder uriToCall = generateInitialPaymentUri(method, amountToPay.getTotalAmountToPay())
                    .queryParam(paymentsAuthChannelIdParam, channelId3d)
                    .queryParam(paymentTransIdParam, amountToPay.getUid())
                    .queryParam("shopperResultUrl", returnToUrl);
            HttpEntity<PaymentResponsePP> request = new HttpEntity<>(stdHeaders);
            logger.info("URL: " + uriToCall.toUriString());
            ResponseEntity<PaymentResponsePP> response = restTemplate.exchange(uriToCall.build().toUri(), HttpMethod.POST,
                    request, PaymentResponsePP.class);
            logger.info("RESPONSE: {}", response.toString());
            PaymentResponsePP paymentResponse = response.getBody();
            amountToPay.setPaymentId(paymentResponse.getThisPaymentId());
            if (StringUtils.isEmpty(paymentResponse.getRedirectUrl())) {
                handleSuccessfulPayment(amountToPay, paymentResponse);
            }
            return paymentResponse;
        } catch (HttpStatusCodeException e) {
            try {
                PaymentErrorPP errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), PaymentErrorPP.class);
                logger.info("Payment Error!: {}", errorResponse.toString());
                throw new PaymentMethodFailedException(errorResponse);
            } catch (IOException error) {
                error.printStackTrace();
                throw new PaymentMethodFailedException(null);
            } finally {
                sendFailureEmail(amountToPay, e.getResponseBodyAsString());
            }
        }
    }

    @Override
    public PaymentResponse asyncPaymentCheckResult(String paymentId, String resourcePath) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(paymentsRestHost)
                .path(resourcePath)
                .queryParam(paymentsAuthUserIdParam, userId)
                .queryParam(paymentsAuthPasswordParam, password)
                .queryParam(paymentsAuthChannelIdParam, channelId3d);

        stdHeaders = new HttpHeaders();
        stdHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        stdHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");

        logger.info("Calling URI: " + builder.toUriString());

        HttpEntity<PaymentResponsePP> request = new HttpEntity<>(stdHeaders);
        ResponseEntity<PaymentResponsePP> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET,
                request, PaymentResponsePP.class);
        logger.info("Async response: {}", response.toString());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody().getResult() != null) {
            PaymentResponsePP responsePP = response.getBody();
            AccountBillingRecord record = billingRepository.findOneByPaymentId(paymentId);
            if (responsePP.getResult().isSuccessful()) {
                handleSuccessfulPayment(record, responsePP);
            } else {
                sendFailureEmail(record, response.getBody().toString());
            }
            return responsePP;
        } else {
            logger.info("Error in response! {}", response.toString());
            sendFailureEmail(null, response.toString());
            return new PaymentResponse(PaymentResultType.FAILED_OTHER, paymentId); // todo : probably need to respond
        }
    }

    private UriComponentsBuilder generateInitialPaymentUri(PaymentMethod paymentMethod, double amountToPay) {
        return baseUriBuilder.cloneBuilder()
                .path(initialPaymentRestPath)
                .queryParam(paymentAmountParam, AMOUNT_FORMAT.format(amountToPay / 100.00))
                .queryParam(paymentCardBrand, paymentMethod.getCardBrand())
                .queryParam(paymentTypeParam, DEBIT)
                .queryParam(cardNumberParam, paymentMethod.normalizedCardNumber())
                .queryParam(cardHolderParam, paymentMethod.getCardHolder())
                .queryParam(cardExpiryMonthParam, String.format(MONTH_FORMAT, paymentMethod.getExpiryMonth()))
                .queryParam(cardExpiryYearParam, String.format(YEAR_FORMAT, paymentMethod.getExpiryYear()))
                .queryParam(securityCodeParam, paymentMethod.getSecurityCode())
                .queryParam(recurringParam, INITIAL)
                .queryParam(registrationFlag, "true");
    }

    @Override
    @Transactional
    public void processAccountPaymentsOutstanding() {
        if (paymentsEnabled) {
            billingRepository.findAll(Specifications
                    .where(paymentDateNotNull())
                    .and(isPaid(false))
                    .and(paymentDateBefore(Instant.now())))
                    .forEach(this::triggerRecurringPayment);
        }
    }

    private boolean triggerRecurringPayment(AccountBillingRecord billingRecord) {
        Account account = billingRecord.getAccount();
        final String recurringPaymentPathVar = String.format(recurringPaymentRestPath, account.getPaymentRef());
        final double amountToPay = (double) billingRecord.getTotalAmountToPay() / 100;
        UriComponentsBuilder paymentUri = baseUriBuilder.cloneBuilder()
                .path(recurringPaymentPathVar)
                .queryParam(paymentAmountParam,  AMOUNT_FORMAT.format(amountToPay))
                .queryParam(paymentTypeParam, RECURRING)
                .queryParam(recurringParam, REPEAT);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<PaymentResponsePP> request = new HttpEntity<>(headers);
            // logger.info("URL: " + paymentUri.toUriString());
            ResponseEntity<PaymentResponsePP> response = restTemplate.exchange(paymentUri.build().toUri(), HttpMethod.POST, request, PaymentResponsePP.class);
            PaymentResponsePP okayResponse = response.getBody();
            logger.info("Payment Success!: {}", okayResponse.toString());

            if (OKAY_CODE.equals(okayResponse.getResult().getCode())) {
                handleSuccessfulPayment(billingRecord, okayResponse);
            }

            return false;
        } catch (HttpStatusCodeException e) {
            handlePaymentError(e);
            return false;
        }
    }

    @Transactional
    private void handleSuccessfulPayment(AccountBillingRecord record, PaymentResponsePP response) {
        Account account = record.getAccount();
        account.setLastPaymentDate(Instant.now());
        account.decreaseBalance(record.getTotalAmountToPay());
        record.setPaid(true);
        record.setPaidDate(Instant.now());
        record.setPaymentId(response.getId());
        record.setPaidAmount(response.getAmount() == null ? 0
                : (long) (response.getAmount() * 100));

        if (emailSendingBroker != null) {
            String email = "A payment succeeded for account " + account.getAccountName() + ", with amount paid as "
                    + (record.getTotalAmountToPay() / 100) + ". Next billing date is " + account.getNextBillingDate();
            emailSendingBroker.sendMail(new GrassrootEmail.EmailBuilder("Payment notification: successful")
                    .content(email)
                    .address(paymentsEmailNotification)
                    .build());
        }
    }

    private PaymentErrorPP handlePaymentError(HttpStatusCodeException e) {
        try {
            PaymentErrorPP errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), PaymentErrorPP.class);
            logger.info("Payment Error!: {}", errorResponse.toString());
            return errorResponse;
        } catch (IOException error) {
            logger.info("Could not read in JSON!");
            error.printStackTrace();
            return null;
        }
    }

    private void sendFailureEmail(AccountBillingRecord record, String errorBody) {
        if (emailSendingBroker != null) {
            String email = "A payment failed.";
            if (record != null) {
                email += "The payment was for " + record.getAccount().getAccountName() + ", with amount paid as "
                        + (record.getTotalAmountToPay() / 100) + ". \n";
            }
            email += "The error body received from the server was: \n" + errorBody;
            emailSendingBroker.sendMail(new GrassrootEmail.EmailBuilder("Payment notification: error!")
                    .content(email)
                    .address(paymentsEmailNotification)
                    .build());
        }
    }

}
