# Companies House Orders API
## orders.api.ch.gov.uk
API handling CRUD operations on CH Ordering Service

### Requirements
* [Java 8][1]
* [Maven][2]
* [Git][3]

### Getting Started
1. Run `make` to build
2. Run `./start.sh` to run

### Environment Variables
Name | Description | Mandatory | Location
--- | --- | --- | ---
ORDERS_API_PORT | Port this application runs on when deployed. | ✓ | start.sh

### Endpoints
Path | Method | Description
--- | --- | ---
*`/basket`* | PATCH | Adds delivery details to `basket`.
*`/basket/items`* | POST | Adds requested `item` to `basket`.
*`/basket/checkouts`* | POST | Processes `basket` checkout. Creates a `checkout` resource.
*`/basket/checkouts/{checkoutId}/payment`* | GET | Returns `paymentDetails` resource for a valid `checkoutId`.
*`/basket/checkouts/{checkoutId}/payment`* | PATCH | Updates `checkout` resource with payment status.
*`/orders/{orderId}`* | GET | Returns `OrderData` resource for a valid `orderId`.
*`/healthcheck`* | GET | Returns HTTP OK (`200`) to indicate a healthy application instance.

[1]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: https://maven.apache.org/download.cgi
[3]: https://git-scm.com/downloads
