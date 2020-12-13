package superyuda.ordering_system.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;


import okhttp3.*;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@AllArgsConstructor
@Component
public class DistanceTime {

    private static final String API_KEY = System.getenv("API_KEY");
    String fixieUrl = System.getenv("FIXIE_URL");
    String[] fixieValues = fixieUrl.split("[/(:\\/@)/]+");
    String fixieUser = fixieValues[1];
    String fixiePassword = fixieValues[2];
    String fixieHost = fixieValues[3];
    int fixiePort = Integer.parseInt(fixieValues[4]);

    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
    Authenticator proxyAuthenticator = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) {
            String credential = Credentials.basic(fixieUser, fixiePassword);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        }
    };


    public String calculate(String source, String destination) throws IOException {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + source + "&destinations=" + destination + "&key=" + API_KEY;
        clientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(fixieHost, fixiePort)))
                .proxyAuthenticator(proxyAuthenticator);

        OkHttpClient client = clientBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public int getTimeValue(String source, String destination) throws IOException {
        String toParse = calculate(source, destination);
        String parsed = toParse.substring(toParse.indexOf("duration"));
        String startPoint = "\"value\" :";
        int charValue = parsed.indexOf(startPoint) + startPoint.length();
        String theNumber = parsed.substring(charValue, parsed.indexOf("}")).trim();
        return Integer.valueOf(theNumber);
    }

    private int benYehudaTime(String destination) throws IOException {
        return getTimeValue("בן יהודה 32 תל אביב", destination);
    }

    private int ibenGabirolTime(String destination) throws IOException {
        return getTimeValue("אבן גבירול 73 תל אביב", destination);
    }

    private int yishayahuTime(String destination) throws IOException {
        return getTimeValue("ישעיהו 1 תל אביב", destination);
    }

    private int bartTime(String destination) throws IOException {
        return getTimeValue("בארט 6 תל אביב", destination);
    }

    private int macabiTime(String destination) throws IOException {
        return getTimeValue("יהודה מכבי 72 תל אביב", destination);
    }

    private int benZionTime(String destination) throws IOException {
        return getTimeValue("בן ציון 3 תל אביב", destination);
    }


    public String getClosestBranch(String destination) throws IOException {
        String tlv = "תל אביב יפו";
        if (destination.contains("תל אביב")) {
            destination = destination.substring(0, destination.indexOf(tlv) + tlv.length());
        }
        if (destination.contains("כניסה")) {
            destination = destination.substring(0, destination.indexOf("כניסה"));
        } else if (destination.contains("קומה")) {
            destination = destination.substring(0, destination.indexOf("קומה"));
        } else if (destination.contains("דירה")) {
            destination = destination.substring(0, destination.indexOf("דירה"));
        } else if (destination.contains("דִירָה")) {
            destination = destination.substring(0, destination.indexOf("דִירָה"));
        } else if (destination.contains("קוד")) {
            destination = destination.substring(0, destination.indexOf("קוד כניסה"));
        }
        if (!destination.contains("תל אביב")) {
            destination = destination.concat(" תל אביב");
        }
        int benYehuda = benYehudaTime(destination);
        int yishayahu = yishayahuTime(destination);
        int ibenGabirol = ibenGabirolTime(destination);
        int macabi = macabiTime(destination);
        int benZion = benZionTime(destination);
        int bart = bartTime(destination);
        if (macabi < benYehuda &&
                macabi < bart &&
                macabi < ibenGabirol &&
                macabi < yishayahu &&
                macabi < benZion &&
                macabi <= 210) {
            return "mac72";
        } else if (benYehuda < bart &&
                benYehuda < ibenGabirol &&
                benYehuda < yishayahu &&
                benYehuda < benZion) {
            return "ben32";
        } else if (bart < ibenGabirol &&
                bart < yishayahu &&
                bart < benZion) {
            return "bart6";
        } else if (ibenGabirol < yishayahu &&
                ibenGabirol < benZion) {
            return "gab73";
        } else if (yishayahu < benZion) {
            return "yis1";
        } else if (benZion < benYehuda &&
                benZion < bart &&
                benZion < ibenGabirol &&
                benZion < yishayahu) {
            return "benZ3";
        }
        return "error";
    }
}