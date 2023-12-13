cd ../
./gradlew gatlingRun-uk.gov.hmcts.juror.performance.simulation.summonsreply.SummonsManagementDeferralSimulation \
-DBASE_URL="http://0.0.0.0:3000" \
-DTEST_TYPE="PERFORMANCE" \
-DENVIRONMENT="PERFORMANCE" \
-DDEBUG="true" \
-DDB_URL="jdbc:postgresql://localhost:5432/juror" \
-DDB_USERNAME="system" \
-DDB_PASSWORD="postgres" \
-DRANK_UP_TIME_SECONDS="5" \
-DRANK_DOWN_TIME_SECONDS="5" \
-DTEST_DURATION_SECONDS="90" \
-DPIPELINE_USERS_PER_SECOND="1" \
-DUSERS_PER_SECOND="2" \
-DREQUESTS_PER_SECOND_PER_USER="1"