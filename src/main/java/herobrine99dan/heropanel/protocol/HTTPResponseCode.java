package herobrine99dan.heropanel.protocol;

public enum HTTPResponseCode 
{
    Code200("200 OK"), 
    Code100("100 Continue"), 
    Code204("204 No Content"), 
    Code301("301 Moved Permanently"), 
    Code307("307 Temporary Redirect"),
    Code308("308 Permanent Redirect"), 
    Code400("400 Bad Request"),
    Code401("401 Unauthorized"), 
    Code403("403 Forbidden"),
    Code404("404 Not Found"), 
    Code405("405 Method Not Allowed"),
    Code406("406 Not Acceptable"), 
    Code410("410 Gone"),
    Code411("411 Length Required"), 
    Code413("413 Payload Too Large"),
    Code414("414 URI Too Long"),
    Code418("418 I'm a teapot"), 
    Code420("420 Method Failure"),
    Code429("429 Too Many Requests"), 
    Code500("500 Internal Server Error"),
    Code501("501 Not Implemented"), 
    Code502("502 Bad Gateway"),
    Code503("503 Service Unavailable"), 
    Code504("504 Gateway Timeout"),
    Code529("529 Site is overloaded"), 
    Code509("509 Bandwidth Limit Exceeded"), 
    Code408("408 Request Timeout");
 
    public String getResponse() {
		return response;
	}

	private String response;
 
    HTTPResponseCode(String response) {
        this.response = response;
    }
}