package asg.concert.service.services;

import asg.concert.common.dto.UserDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/concert-service/login")
public class LoginResource {

    private static Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    private static ArrayList<String> validCookies = new ArrayList<>();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO userDTO, @CookieParam(Config.CLIENT_COOKIE) Cookie clientId){
        LOGGER.info("Trying to login with cookie: " + clientId);
        EntityManager em = PersistenceManager.instance().createEntityManager();
        User user = null;

        try {

            // Start a new transaction.
            em.getTransaction().begin();

            // Use the EntityManager to retrieve, persist or delete object(s).
            // Use em.find(), em.persist(), em.merge(), etc...
            TypedQuery<User> userQuery = em.createQuery("select u from User u where Username='" + userDTO.getUsername() + "'", User.class);
            List<User> users = userQuery.getResultList();

            // there should only ever be one user with a given username
            // check the passowrd matches
            if (users.size() == 1 && users.get(0).getPassword().equals(userDTO.getPassword())) {
                user = users.get(0);
            }
            // Commit the transaction.
            em.getTransaction().commit();
        } finally {
            // When you're done using the EntityManager, close it to free up resources.
            em.close();
        }
        if (user == null){
            return Response.status(401).build();
        }

        return Response
                .status(200)
                .cookie(makeCookie(clientId))
                .build();
    }

    private NewCookie makeCookie(Cookie clientId) {
        NewCookie newCookie = null;
        if (isCookieValid(clientId)){
            clientId = null;
        }
        if (clientId == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
            validCookies.add(newCookie.getValue());
        }

        return newCookie;
    }

    public static boolean isCookieValid(Cookie clientId) {
        if (clientId == null) {
            return false;
        }

        return validCookies.contains(clientId.getValue());
    }
}
