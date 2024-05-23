cd ../
./gradlew gatlingRun \
-DBASE_URL="http://0.0.0.0:3000" \
-DTEST_TYPE="LOCAL" \
-DENVIRONMENT="PERFORMANCE" \
-DDEBUG="true" \
-DDB_URL="jdbc:postgresql://localhost:5432/juror" \
-DDB_USERNAME="system" \
-DDB_PASSWORD="postgres"