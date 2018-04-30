import java.net.URLEncoder

class SonarApiClient
{
    static def sonarApiUrl = 'https://sonar.dev-build-create.build.finkit.io/api/'

    private static String encode(value) {
        return URLEncoder.encode(value, 'UTF-8')
    }

    static def buildSingleValuedKeyPair(String key, String value) {
        return encode(key) + '=' + encode(value)
    }

    static def buildMultiValuedKeyPair(String key, Iterator<String> values) {
        if (!values.hasNext()) {
            return buildSingleValuedKeyPair(key, values.first)
        }
    
        def keyPair = encode(key) + '=' + encode(values.first)

        values.next

        for (value in values) {
            keyPair += '&' + encode(key) + '=' + encode(value)
        }
    
        return keyPair
    }

    static def buildQueryString(Iterator<String> keyValuePairs) {
        if (!keyValuePairs.hasNext()) {
            return ''
        }

        def pair = keyValuePairs.next()

        for (keyValuePair in keyValuePairs) {
            pair += '&' + keyValuePair
        }

        return pair
    }

    private static String generateAuthValue() {
        def username = 'admin'
        def password = 'admin'
    
        return "${username}:${password}".getBytes().encodeBase64().toString()
    }
    
    static boolean postQueryString(String url, String queryString) {
        def connection = new URL(url).openConnection() as HttpURLConnection
    
        connection.setRequestProperty('Accept', 'application/json')
        connection.setRequestProperty('Authorization', "Basic ${generateAuthValue()}")
        connection.setRequestMethod('POST')
        connection.doOutput = true
    
        def writer = new OutputStreamWriter(connection.outputStream)
        
        try {
            writer.write(queryString)
            writer.flush()
        
            if (connection.responseCode == HttpURLConnection.HTTP_OK ||
                connection.responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                println "Request to ${url} with query string ${queryString} succeeded"
            } else {
                println "Request to ${url} with query string ${queryString} failed with status " +
                        "${connection.responseCode} and response ${connection.responseMessage}"
                return false
            }
        } catch (IOException ioe) {
            println "Request failed with error: ${ioe}"
            return false
        } finally {
            writer.close()
        }
    
        return true
    }
}
