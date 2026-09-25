Java.perform(function () {
  // 1. TrustManager Bypass (Universal)
  try {
    var TrustManager = Java.registerClass({
      name: "com.custom.TrustManager",
      implements: [Java.use("javax.net.ssl.X509TrustManager")],
      methods: {
        checkClientTrusted: function () {},
        checkServerTrusted: function () {},
        getAcceptedIssuers: function () {
          return [];
        },
      },
    });
    var SSLContextInit = Java.use("javax.net.ssl.SSLContext").init.overload(
      "[Ljavax.net.ssl.KeyManager;",
      "[Ljavax.net.ssl.TrustManager;",
      "java.security.SecureRandom"
    );
    SSLContextInit.implementation = function (k, t, r) {
      SSLContextInit.call(this, k, [TrustManager.$new()], r);
    };
  } catch (e) {}

  // 2. HostnameVerifier Bypass (OkHttp)
  try {
    var OkHV = Java.use("okhttp3.internal.tls.OkHostnameVerifier");
    var returnTrue = function () {
      return true;
    };
    OkHV.verify.overload(
      "java.lang.String",
      "javax.net.ssl.SSLSession"
    ).implementation = returnTrue;
    OkHV.verify.overload(
      "java.lang.String",
      "java.security.cert.X509Certificate"
    ).implementation = returnTrue;
  } catch (e) {}

  // 3. CertificatePinner Bypass (Builder + Dynamic)
  try {
    var Pinner = Java.use("okhttp3.CertificatePinner");
  
    Java.use("okhttp3.CertificatePinner$Builder").build.implementation =
      function () {
        return Pinner.DEFAULT.value;
      };

    var methods = Pinner.class.getDeclaredMethods();
    for (var i = 0; i < methods.length; i++) {
      if (methods[i].getName() === "check") {
        var args = methods[i].getParameterTypes().map(function (p) {
          return p.getName();
        });
        var overload = Pinner.check.overload.apply(Pinner.check, args);
        overload.implementation = function () {
          return;
        };
      }
    }
  } catch (e) {}
  console.log("[+] SSL Bypass Active");
});


