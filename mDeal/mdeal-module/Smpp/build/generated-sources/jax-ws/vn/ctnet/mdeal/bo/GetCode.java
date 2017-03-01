
package vn.ctnet.mdeal.bo;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "GetCode", targetNamespace = "http://tempuri.org/", wsdlLocation = "http://ws.mdeal.vn/Webservice/GetCode.asmx?wsdl")
public class GetCode
    extends Service
{

    private final static URL GETCODE_WSDL_LOCATION;
    private final static WebServiceException GETCODE_EXCEPTION;
    private final static QName GETCODE_QNAME = new QName("http://tempuri.org/", "GetCode");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://ws.mdeal.vn/Webservice/GetCode.asmx?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        GETCODE_WSDL_LOCATION = url;
        GETCODE_EXCEPTION = e;
    }

    public GetCode() {
        super(__getWsdlLocation(), GETCODE_QNAME);
    }

    public GetCode(WebServiceFeature... features) {
        super(__getWsdlLocation(), GETCODE_QNAME, features);
    }

    public GetCode(URL wsdlLocation) {
        super(wsdlLocation, GETCODE_QNAME);
    }

    public GetCode(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, GETCODE_QNAME, features);
    }

    public GetCode(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public GetCode(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns GetCodeSoap
     */
    @WebEndpoint(name = "GetCodeSoap")
    public GetCodeSoap getGetCodeSoap() {
        return super.getPort(new QName("http://tempuri.org/", "GetCodeSoap"), GetCodeSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns GetCodeSoap
     */
    @WebEndpoint(name = "GetCodeSoap")
    public GetCodeSoap getGetCodeSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://tempuri.org/", "GetCodeSoap"), GetCodeSoap.class, features);
    }

    /**
     * 
     * @return
     *     returns GetCodeSoap
     */
    @WebEndpoint(name = "GetCodeSoap12")
    public GetCodeSoap getGetCodeSoap12() {
        return super.getPort(new QName("http://tempuri.org/", "GetCodeSoap12"), GetCodeSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns GetCodeSoap
     */
    @WebEndpoint(name = "GetCodeSoap12")
    public GetCodeSoap getGetCodeSoap12(WebServiceFeature... features) {
        return super.getPort(new QName("http://tempuri.org/", "GetCodeSoap12"), GetCodeSoap.class, features);
    }

    private static URL __getWsdlLocation() {
        if (GETCODE_EXCEPTION!= null) {
            throw GETCODE_EXCEPTION;
        }
        return GETCODE_WSDL_LOCATION;
    }

}
