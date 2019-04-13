[![Maintainability](https://api.codeclimate.com/v1/badges/8ff1b518d9455735db7b/maintainability)](https://codeclimate.com/github/configrd/configrd-service/maintainability)[![CircleCI](https://circleci.com/gh/configrd/configrd-service.svg?style=svg)](https://circleci.com/gh/configrd/configrd-service)

See the full documentation at https://configrd.io

# Quick Start

### Pull & Run

Pull the docker image and start the service and map a volume to `/srv/configrd` on both container and host

```bash
docker run -d -p 9191:9191 -v /srv/configrd:/srv/configrd configrd/configrd-service:latest
```

A default `configrd.yaml` file will be created at `/srv/configrd` on your local file system. The service will listen to requests on port 9191.

{% code-tabs %}
{% code-tabs-item title="configrd.yaml" %}
```yaml
service:
  defaults:
    fileName: default.properties
  repos:
    default:
      uri: file:/srv/configrd
      sourceName: file
```
{% endcode-tabs-item %}
{% endcode-tabs %}

Now open your favorite browser and navigate to [http://localhost:9191/configrd/v1/](http://localhost:9191/configrd/v1/)
