# Sample Logging to Service Account

## Logback.xml

```xml
    <appender name="CLOUD" class="com.quadstingray.logging.logback.google.cloud.LoggingAppender">
        <projectId>your-project-id</projectId> <!-- Optional : default java.log -->
        <credentialType>SERVICE_ACCOUNT</credentialType> <!-- Options: FILE, SERVICE_ACCOUNT Optional : default FILE -->
        <clientId>YOUR_CLIENT_ID</clientId> <!-- Required, when used file credentialType -->
        <clientEmail>clientMail@PROJECTID.iam.gserviceaccount.com</clientEmail> <!-- Required, when used file credentialType -->
        <privateKeyId>YOUR_PRIVATE_KEY</privateKeyId> <!-- Required, when used file credentialType -->
        <privateKey>-----BEGIN PRIVATE KEY-----
                    MIIEuwIBadANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQCnxybW5j+m/bvp
                    Stg/XE8g5CbN4pUUR9JlVRF8UaiDDtUi0xSSEO9i7qhCnMYFWpAdhtCEhhrOlzxC
                    yGxXr1seKwaRvVOijgYFjAxDRD57LI7yCQcuSz/MrYuwIuDxHlBwgovPNAD2qS+H
                    61VDcgUV8YJMutvcss85+vDs+ZcjkoLcPkbfPIMp0F6tBydcCzsOqNTbEtevx4Q0
                    JFxFZjGFaUdX+u4zxSFOz85qCD2RTbo+a+NIv3q8vEwiBv/zrqZ3e0Xvpfl+EoFz
                    QNCP/DGe2baBAlDa2KiLfeJww42H04iPCMyRC77pBXKGC23RYhXzdw0pGPcOfF9P
                    gvRHk6I3AgMBAAECgf825tAC+o41WtvVVxi4J9bMruu+PdpIYe+PGWmDp6bD+cxM
                    2cwCnjXj0ukGyeq56+PxB+vOzZTxwyADPz0xoJCdj2b3UDEJTd7Z+1D4Jv+HMoQM
                    Wk0TytF8fHbMY7nl16U7sKV0i9cA7ND31wjO1Q1v2a9y0XnB6Syz8+agdb0db8kZ
                    Mzcl5HEE/ZuB/ayAdeyGGL5f1iu+Jm6k7IvYyPMUWLmdDwcXNYl1dUonMDKBsq9z
                    RR+oInycEnxAaNgk4Fj6oKDRRpL9iHfozl3pWXECgYEA2QIRaDWpYyR9uKdRaP79
                    2djSfupywf1r+YWGguzLiVeqft5SKSOfIjR9aSoac2AvqzJuDVvan9NZ5YKHwWTx
                    +3RLT5ikIaYRSBI+W8Jd6/ECgYEAxeyY0CIOy5jyCklXseHwOiYQj1EZrrs1z4l2
                    nJJuLX6M2vURuRyAFIYIPX6QdG0UsJ6wQxn4PNa1iqhYajFd2qfP+ZV9WOVkVTOL
                    qWJzMqmoOjNZeNWWZ0pSlmSE/+D4KJKx26mZ6BgJQfl3zQCuJHvaCN3r+AAOnNt4
                    J7XSOKcCgYEAj1b/Wn8/kL400PisHJd55CCFAdIo1Rxo6tuY+vgghWrDsqN1T7k4
                    zuYvBH0MFbOuKP3ZlbdblICNe70ZoNPllJeJAN4ho10VkKHtnTsGsuQc/CC+5pe0
                    8eKeLXvv2loS7kBhHWJHlkwFaCMInjA+4BJ0AHKJz7Qa7CkoyYF8enECgYAmenHW
                    /NgNguWDVHDlFzzBwUa05hptGt5CQ8I4fEtJMvIlW0Gf0EeiNdCCTAm/aLl+AMvZ
                    r8HP6hoPI91Uf9Z0PO204vgkgSw5WDUNsCMfNVaMQhFh6Xw9Bnj53f7xVAZKtQI4
                    50cRlSKvndVJmJlcziWl9ab7Zt9wZUZ9yd5dvwKBgBzpiOIBzyHvUf7wCcPUrNhA
                    RIPGAscz6N3UbWidVddEvvA53HtGKlAyfKjYI1KvksX0eN7o9YnGToTLEflwKo+Y
                    UHL0vKx/EplKgN100387wyZsEEpjYzKX/poT/OeNG6OBDvhIYeqsLSBr7KJ05eod
                    1HTCxigP1CpHT+rzstOE
                    -----END PRIVATE KEY-----</privateKey> <!-- Required, when used file credentialType -->
        <log>application2.log</log> <!-- Optional : default java.log -->
    </appender>

```