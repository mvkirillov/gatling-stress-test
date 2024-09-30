package com.kirillov;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SingleUrlStressTestSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8610/lobby")
            .disableCaching()
            .shareConnections();

    ChainBuilder delayUrl = exec(
            http("delay")
                    .get("/test/delay")
                    .check(status().is(200))
    );

    ScenarioBuilder myScenario = scenario("stress-test").exec(delayUrl);

    int targetRps = 280;
    Duration testTime = Duration.ofMinutes(30);

    {
        setUp(myScenario
                .injectOpen(constantUsersPerSec(targetRps)
                        .during(testTime.multipliedBy(4).dividedBy(3)))
                .throttle(
                        reachRps(targetRps).during(testTime.dividedBy(3)),
                        holdFor(testTime)
                )
        ).protocols(httpProtocol);
    }

}
