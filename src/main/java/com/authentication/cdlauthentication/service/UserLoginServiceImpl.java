package com.authentication.cdlauthentication.service;import com.authentication.cdlauthentication.data.UserLoginRequestDto;import com.authentication.cdlauthentication.data.UserLoginResponse;import com.authentication.cdlauthentication.util.security.exceptions.NoAuthorizationException;import com.authentication.cdlauthentication.util.security.exceptions.ResourceNotFoundException;import com.google.gson.*;import lombok.RequiredArgsConstructor;import org.apache.commons.lang3.StringUtils;import org.json.XML;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Value;import org.springframework.context.annotation.ComponentScan;import org.springframework.stereotype.Service;import org.springframework.web.reactive.function.client.WebClient;import org.springframework.web.reactive.function.client.WebClientException;import reactor.core.publisher.Mono;import java.time.Duration;import java.util.Date;@ComponentScan@RequiredArgsConstructor@Servicepublic class UserLoginServiceImpl implements UserLoginService {    private Gson gsonConverter;    JsonObject jsonObject = new JsonObject();    private final UserLoginResponse userLoginResponse = new UserLoginResponse();    private final WebClient webClient= WebClient.builder().build();    private static final Logger LOG = LoggerFactory.getLogger(UserLoginServiceImpl.class);    @Value( "${CDL_FCMB_LDAP_BASE_URL_VALUE}" )    private String uri;    @Value("${CDL_FCMB_LDAP_APP_ID_VALUE}")    private String appIdValue;    @Value("${CDL_FCMB_LDAP_APP_VALUE}")    private String appValue;    @Override    public UserLoginResponse callFCMBApi(UserLoginRequestDto userLoginRequestDto) throws ResourceNotFoundException {        String resultFromFCMB =null;    try {     resultFromFCMB = webClient.post().uri(uri).header("Content-Type", "text/xml").body(Mono.just(                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n"                        + "   <soapenv:Header/>\n" + "   <soapenv:Body>\n" + "      <tem:GetUserAdStaffID>\n"                            + "         <!--Optional:-->\n" + "         <tem:LoginName>" + userLoginRequestDto.getUsername() + "</tem:LoginName>\n"                            + "         <!--Optional:-->\n" + "         <tem:Password>" + userLoginRequestDto.getPassword() + "</tem:Password>\n"                            + "         <!--Optional:-->\n" + "         <tem:AppID>" + appIdValue + "</tem:AppID>\n"                            + "         <!--Optional:-->\n" + "         <tem:AppKey>" + appValue + "</tem:AppKey>\n"                            + "      </tem:GetUserAdStaffID>\n" + "   </soapenv:Body>\n" + "</soapenv:Envelope>"),            String.class).retrieve().bodyToMono(String.class)             .timeout(Duration.ofMinutes(2)).block();    }catch(IllegalArgumentException e){        LOG.info("------> result fcmb 1");        userLoginResponse.setResponseMessage("illegal argument");        userLoginResponse.setDate(new Date());        userLoginResponse.setResponse("404");        LOG.info("response "+userLoginResponse.getResponseMessage());        return userLoginResponse;    }catch (WebClientException r){        LOG.info("-----> result fcmb 2");        userLoginResponse.setResponse("408");        userLoginResponse.setResponseMessage("error from webclient");        userLoginResponse.setDate(new Date());        LOG.info("response "+userLoginResponse.getResponseMessage());        LOG.info("Web client exception: "+r.getMessage());        return userLoginResponse;}        if (resultFromFCMB == null){            throw new IllegalArgumentException("No user found for "+ userLoginRequestDto.getUsername());        }        if (StringUtils.isBlank(resultFromFCMB) || StringUtils.isEmpty(resultFromFCMB)) {            LOG.info("fcmbTemplateElementUserDetails: {}", resultFromFCMB);            LOG.info("First Trial Fail");            throw new NoAuthorizationException("Network error, try again");        }        // converts XML to json        org.json.JSONObject jsonResult = XML.toJSONObject(resultFromFCMB);        final JsonElement fcmbTemplateElementResult = JsonParser.parseString(jsonResult.toString());        final JsonElement fcmbTemplateElementEnvelopeResult = extractJsonObjectNamed("soap:Envelope",                fcmbTemplateElementResult);        final JsonElement fcmbTemplateElementBodyResult = extractJsonObjectNamed("soap:Body",                fcmbTemplateElementEnvelopeResult);        final JsonElement fcmbTemplateElementUserDetailsAd = extractJsonObjectNamed("GetUserAdStaffIDResponse",                fcmbTemplateElementBodyResult);        LOG.info("fcmbTemplateElementUserDetails: {}", fcmbTemplateElementUserDetailsAd.toString());        final String getResult = extractStringNamed("GetUserAdStaffIDResult", fcmbTemplateElementUserDetailsAd);        LOG.info(getResult);        if(getResult == null){            LOG.info("Staff Id --->{}"+getResult);            throw new NoAuthorizationException("No user found for "+ userLoginRequestDto.getUsername());        }        if (!StringUtils.isNumeric(getResult)) {            final String removeLeftPaddingCharacter = getResult.substring(1);// remove first letter            LOG.info("removeLeftPaddingCharacter: {}", removeLeftPaddingCharacter);            if (StringUtils.isNotBlank(removeLeftPaddingCharacter) && !StringUtils.isNumeric(removeLeftPaddingCharacter)) {//                userLoginResponse.setUsername(userLoginRequestDto.getUserName());                throw new ResourceNotFoundException("Incorrect username and password");            }        }        // Making a second call        try {            resultFromFCMB = webClient.post().uri(uri).header("Content-Type", "text/xml")                    .body(Mono.just("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "                            + "xmlns:tem=\"http://tempuri.org/\">\r\n   " + "<soapenv:Header/>\r\n   " + "<soapenv:Body>\r\n      "                            + "<tem:GetUserAdFullDetails>\r\n            " + "<!--Optional:-->\r\n         " + "<tem:LoginName>"                            + userLoginRequestDto.getUsername() + "</tem:LoginName>\r\n         " + "<!--Optional:-->\r\n         " + "<tem:Password>"                            + userLoginRequestDto.getPassword() + "</tem:Password>\r\n         " + "<!--Optional:-->\r\n       " + "  <tem:AppID>"                            + appIdValue + "</tem:AppID>\r\n       " + "  <!--Optional:-->\r\n   " + "      <tem:AppKey>"                            + appValue + "</tem:AppKey>\r\n     " + " </tem:GetUserAdFullDetails>\r\n "                            + "  </soapenv:Body>\r\n" + "</soapenv:Envelope>"), String.class)                    .retrieve().bodyToMono(String.class)                    .timeout(Duration.ofMinutes(2)).block();            LOG.info("Second resultFromFCMB: {}", resultFromFCMB);        }catch (WebClientException er){            LOG.info("-----> result fcmb 2");            userLoginResponse.setResponse("408");            userLoginResponse.setResponseMessage("error from webclient");            userLoginResponse.setDate(new Date());            LOG.info("response "+userLoginResponse.getResponseMessage());            LOG.info("Web client exception: "+er.getMessage());            return userLoginResponse;        }        if (StringUtils.isBlank(resultFromFCMB) || StringUtils.isEmpty(resultFromFCMB)) {            LOG.info("Second Trial Fail");            throw new NoAuthorizationException("Network error, try again");        }        org.json.JSONObject json = XML.toJSONObject(resultFromFCMB);        final JsonElement fcmbTemplateElement = JsonParser.parseString(json.toString());        final JsonElement fcmbTemplateElementEnvelope = extractJsonObjectNamed("soap:Envelope",                fcmbTemplateElement);        final JsonElement fcmbTemplateElementBody = extractJsonObjectNamed("soap:Body",                fcmbTemplateElementEnvelope);        final JsonElement fcmbTemplateElementUserDetails = extractJsonObjectNamed("GetUserAdFullDetailsResponse",                fcmbTemplateElementBody);        final JsonElement fcmbTemplateElementUserDetailsResult = extractJsonObjectNamed("GetUserAdFullDetailsResult", fcmbTemplateElementUserDetails);        final String email = extractStringNamed("Email", fcmbTemplateElementUserDetailsResult);        final String staffID = extractStringNamed("StaffID", fcmbTemplateElementUserDetailsResult);        final String department = extractStringNamed("Department", fcmbTemplateElementUserDetailsResult);        final String mobileNo = extractStringNamed("MobileNo", fcmbTemplateElementUserDetailsResult);        final String displayName = extractStringNamed("DisplayName", fcmbTemplateElementUserDetailsResult);        final String staffName = extractStringNamed("StaffName", fcmbTemplateElementUserDetailsResult);        final String response = extractStringNamed("Response", fcmbTemplateElementUserDetailsResult);        final String responseMessage = extractStringNamed("ResponseMessage",                fcmbTemplateElementUserDetailsResult);        if (response.equals("00")) {            final String Department = extractStringNamed("Department", fcmbTemplateElementUserDetailsResult);            if (StringUtils.isBlank(Department)) {                throw new RuntimeException("No Department exists for this user");            }        }        UserLoginResponse response1 = UserLoginResponse.builder()                .staffID(Integer.parseInt(staffID))                .displayName(displayName)                .department(department)                .email(email)                .mobileNo(mobileNo)                .staffName(staffName)                .response(response)                .responseMessage(responseMessage)                .build();        LOG.info("json result: {}", jsonObject);        return response1;    }    private JsonObject extractJsonObjectNamed(final String parameterName, final JsonElement element) {        JsonObject jsonObject = null;        if (element.isJsonObject()) {            final JsonObject object = element.getAsJsonObject();            if (object.has(parameterName)) {                jsonObject = object.get(parameterName).getAsJsonObject();            }        }        return jsonObject;    }    private String extractStringNamed(final String parameterName, final JsonElement element) {        String stringValue = null;        if (element.isJsonObject()) {            final JsonObject object = element.getAsJsonObject();            if (object.has(parameterName) && object.get(parameterName).isJsonPrimitive()) {                final JsonPrimitive primitive = object.get(parameterName).getAsJsonPrimitive();                final String valueAsString = primitive.getAsString();                if (StringUtils.isNotBlank(valueAsString)) {                    stringValue = valueAsString;                }            }        }        return stringValue;    }    private static boolean isPayloadValidated(String request){        return StringUtils.isNotBlank(request) && StringUtils.isNotEmpty(request);    }}