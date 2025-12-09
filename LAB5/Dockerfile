# Используем официальный образ Tomcat
FROM tomcat:9.0-jre11

# Удаляем дефолтные приложения Tomcat (опционально)
RUN rm -rf /usr/local/tomcat/webapps/*

# Копируем ваш WAR-файл в контейнер
COPY target/your-app.war /usr/local/tomcat/webapps/ROOT.war

# Можно также копировать в отдельную папку:
# COPY target/your-app.war /usr/local/tomcat/webapps/yourapp.war

# При необходимости добавляем конфигурации
# COPY config/ /usr/local/tomcat/conf/

# Экспортируем порт Tomcat
EXPOSE 8080

# Команда запуска Tomcat
CMD ["catalina.sh", "run" ]