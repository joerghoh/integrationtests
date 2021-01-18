# Sample integration tests with AEM

This AEM project is a demo how integration tests can be implemented with AEM. Therefor the focus is only on the maven module ```it.tests``` and some adjustments to the ```dispatcher``` module; all other modules remain unchanged.

There is also a series of blog posts about AEM integration testing available, see https://cqdump.wordpress.com/integration-tests-with-aem/ for an overview. I recommend you to start there.

If your goal is to quickly execute the integration tests, use this:

* start with AEM 6.5 or the AEM SDK (for AEM as a Cloud Service)
* Make sure that you have the WKND democontent on author and publish
* Clone this repository
* Adjust the parameters (hostname & port) in [pom.xml of the integration tests](it.tests/pom.xml#L98) to match your environments.
* start the dispatcher docker image (see https://experienceleague.adobe.com/docs/experience-manager-learn/cloud-service/local-development-environment-set-up/dispatcher-tools.html) and use the configuration of the dispatcher module of this repository.
* Run the command ```mvn clean install -Peaas-local``` in the folder ```it.tests``` to execute the integration tests.

