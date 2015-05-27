package de.morpheus.chatbot.chatbotapp;

import java.io.EOFException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class WebService {

    private final String NAMESPACE = "http://service.chatbot.morpheus.de";
    private final String URL = "http://194.95.221.229:8080/chatbot_service_tomcat/services/ChatbotService?wsdl";
    private final String SOAP_ACTION = "http://service.chatbot.morpheus.de/communicate";
    private final String METHODNAME = "communicate";
    private final int TIMEOUT = 1500;
    private SoapObject request;
    private HttpTransportSE http;
    private String result;

	protected String sendMessageToChatbot(String input) {
		
        request = new SoapObject(NAMESPACE, METHODNAME);
        request.addProperty("input", input);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        http = new HttpTransportSE(URL, TIMEOUT);
        http.debug = true;

        try {
        	
            try {
            	http.call(SOAP_ACTION, envelope);
            }
            catch(EOFException exception) {
            	http.call(SOAP_ACTION, envelope);
            }
        	
            try {
                SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
                result= String.valueOf(response.toString());
            }
            catch(ClassCastException exception) {
            	RecognitionService.callSuccessful = false;
            }
        }
        catch(Exception exception) {
        	exception.printStackTrace();
        }

        return result;
	}
}