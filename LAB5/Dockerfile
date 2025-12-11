FROM tomcat:11.0-jre17
COPY target/lab5-api.war /usr/local/tomcat/webapps/lab5-api.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=LAB_5
ENV DB_USER=postgres
ENV DB_PASSWORD=A2D5T97YU8
