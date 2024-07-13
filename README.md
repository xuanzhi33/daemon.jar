# daemon.jar
Automatically deploy and update the server using Github Actions.

## Usage

1. Install JDK 21.
2. Download the latest version from the [releases](https://github.com/xuanzhi33/daemon.jar/releases/latest).
3. Create a `application-prod.yml` file in the same directory as the jar file.
```yaml
server:
    port: 8346 # Port

daemonJar:
    url: https://github.com/xuanzhi33/daemon.jar/releases/download/v0.2.0/daemonjar-0.2.0.jar # URL to download your server program
    name: daemonjar-0.2.0.jar # Name of it
    token: your_token # Update Token
```
4. Run the jar file.
```shell
java -jar daemonjar-x.x.x.jar
```
5. Using the following command to update your server.
```shell
curl https://your-ip:port/update?token=your_token
```
6. It will download the latest version of the server program and restart it.