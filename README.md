# camel-retry
[![Build Status](https://travis-ci.org/pvalsecc/camel-retry.svg)](https://travis-ci.org/pvalsecc/camel-retry)

A Camel Component to handle retries. Retries can be done
[using error handlers](http://camel.apache.org/how-do-i-retry-processing-a-message-from-a-certain-point-back-or-an-entire-route.html).
But this works only if you don't want to have two levels of retries.

This component is a workaround for that.

Have a look at the unittests for examples of usage.