package org.advisor;

import org.advisor.member.jwt.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@SpringBootTest
public class BeanInitializationTest {

    @Autowired
    private TokenService tokenService;

    @Test
    void testTokenServiceBean() {
        assertNotNull(tokenService, "TokenService Bean should be initialized");
    }
}
