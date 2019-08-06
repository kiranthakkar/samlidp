# samlidp

The SAML IDP s a compact, easy to deploy SAML 2.0 Identity Provider implementation. The use case is any J2EE application that requires Identity provider implementation or anyone who wants to extend the service provider's authentication functionality like that of IDCS can use it. It uses opensaml for SAML functionality. Apart from that, it has a Login interface that users can implement to support authentication factors that the service provider does not support Out-Of the Box. The default implementation asks for username and creates SAML assertion for that user

SAML IDP can accept SAML 2.0 authentication requests from any SAML 2.0 Service Provider, authenticate the user, and then send the user back to a service provider with SAML assertion. It implements the POST profile of SAML 2.0.

Health warning: This code is not meant to be used in PROD environment. If you choose to do so, please do extensive testing including security review. Even Readme is first cut. I will make more changes to this and make this more usable.

The codebase is maintained on orahub﻿. 

Here are the steps to download the bundle and deploy it. It does not include any container. So you clone the package and create .war file.

git clone git@orahub.oraclecorp.com:ateam/samlidp.git
cd samlidp
mvn clean compile war:war (Make sure you have Maven installed and configured.)
It will create SAMLClient.war file under the target directory. Extract SAMLClient.war file and update partners.xml to add service provider partner you want to integrate with.

AssertionConsumerURL: This is the URL where IDP has to send SAML assertion post-authentication. SAMLRecipient: In most cases, this will be same as assertion consumer URL.

ProviderID: This is the provider ID of service provider. SAMLAudience: In most cases, this will be same as ProviderID. However, in some cases, it can be different. For example, for salesforce sandbox environments, SAMLAudience and providerID are different.

RelayState: If you do IDP initiated SSO by hitting /Login servlet then IDP will send this URL as RelayState URL.

NameIDFormat: Format of NameID in the SAML assertion.

Once this config is done, configure the service provider. The codebase has certs directory that has cert.pem. That is the certificate you should import on the service provider as IDP certificate.