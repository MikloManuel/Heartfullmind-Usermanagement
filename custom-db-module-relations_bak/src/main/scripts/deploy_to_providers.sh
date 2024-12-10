export KEYCLOAK_ROOT_DIR="/home/manuel/MMMM/HeartfullMind/HumanResourceManagement";
export KEYCLOAK_HOME="$KEYCLOAK_ROOT_DIR/quarkus/server/target";
export JAVA_HOME="/home/manuel/.sdkman/candidates/java/current";
export MAVEN_HOME=/opt/maven/apache-maven-3.9.8
export M2_HOME=$MAVEN_HOME
export PATH=$MAVEN_HOME/bin:$PATH
export MAVEN_OPTS="-Xmx2048m"

cd "$KEYCLOAK_ROOT_DIR/custom-db-module-relations/";

mv "$KEYCLOAK_ROOT_DIR/custom-db-module-relations/target/keycloak-custom-db-999.0.0-SNAPSHOT.jar" "$KEYCLOAK_ROOT_DIR/quarkus/server/target/providers/keycloak-custom-db-999.0.0-SNAPSHOT.jar"