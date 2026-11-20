import os

# Django espera el módulo "MySQLdb" para hablar con MySQL. PyMySQL es una
# alternativa en Python puro (fácil de instalar en Windows) que se hace pasar
# por MySQLdb con esta línea.
if os.environ.get("DB_ENGINE", "mysql") == "mysql":
    import pymysql

    pymysql.install_as_MySQLdb()
