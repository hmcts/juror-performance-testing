package uk.gov.hmcts.juror.performance.scenario;

import io.gatling.javaapi.core.ChainBuilder;
import uk.gov.hmcts.juror.performance.Feeders;
import uk.gov.hmcts.juror.performance.Util;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.substring;
import static io.gatling.javaapi.http.HttpDsl.http;
import static uk.gov.hmcts.juror.performance.Util.DEFAULT_THINK_TIME_MS;

public class PrintLetterScenario {


    public static ChainBuilder postPrintLetter(String titlePrefix,
                                               String letterType,
                                               String letterTypeUrl,
                                               String overviewSubStringCheck,
                                               String pageIdentifer
                                               ) {
        return  exec(session -> session.set("printLetters", Feeders.YES_NO_GEN.generate()))
            .doIfOrElse(session -> {
                String printLetters = session.getString("printLetters");
                assert printLetters != null;
                return printLetters.equals("no");
            })
            .then(http(titlePrefix + " - Print letters - No")
                .post("/juror-management/juror/#{juror_number}/update/"+letterTypeUrl+"/letter")
                .headers(Util.COMMON_HEADERS)
                .formParam("printLetters", "no")
                .formParam("letterType", letterType)
                .formParam("_csrf", "#{csrf}")
                //Post code does not auto redirect this is done via JS so page should be the same
                .check(Util.validatePageIdentifier("Juror record - Overview"))
                .check(substring(overviewSubStringCheck)))
            .orElse(http(titlePrefix + " - Print letters - Yes")
                    .post("/juror-management/juror/#{juror_number}/update/"+letterTypeUrl+"/letter")
                    .headers(Util.COMMON_HEADERS)
                    .formParam("printLetters", "yes")
                    .formParam("letterType", letterType)
                    .formParam("_csrf", "#{csrf}")
                    //Post code does not auto redirect this is done via JS so page should be the same
                    .check(Util.validatePageIdentifier(pageIdentifer + " - Redirect to print flow")),
                //Get overview page and check for deferral granted
                http("GET - juror record overview")
                    .get("/juror-management/record/#{juror_number}/overview")
                    .headers(Util.COMMON_HEADERS)
                    .check(Util.validatePageIdentifier("Juror record - Overview"))
                    .check(substring(overviewSubStringCheck))
            ).pause(Duration.ofMillis(DEFAULT_THINK_TIME_MS));
    }

}
