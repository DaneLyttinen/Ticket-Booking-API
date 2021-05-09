package asg.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.concert.common.dto.UserDTO;
import asg.concert.service.common.Config;
import asg.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
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


    /**
     * The user gives a username and password (via UserDTO) and then the webserver
     * checks if a user with that username and password exist; if so, the webserver
     * gives them a cookie (which stores a 'token' that the webserver generates
     * for that user) as a means to give them a token which they can send in subsequent
     * requests to the web server to authenticate themselves (as opposed to sending 
     * their username and password each time). This is called token based authentication.
     * 
     * Having a cookie is a particular nice means to exchange a token, as
     * HTTP works such that this cookie will be automatically sent in this user's
     * subsequent requests to the webserver.
     * 
     * If user does not exist with that username, or password incorrect: throws 
     * Response.Status.Unauthorized (more like "unauthenticated") 
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO userDTO){
        LOGGER.info(String.format("Logging in user with username '%s'...", userDTO.getUsername()));

        EntityManager em = PersistenceManager.instance().createEntityManager();

        String token = null;

        try {
            em.getTransaction().begin();

            String queryString = String.format("select u from User u where u.username = '%s'", userDTO.getUsername());
            TypedQuery<User> userQuery = em.createQuery(queryString, User.class).setLockMode(LockModeType.PESSIMISTIC_READ);
            List<User> usersWithUsername = userQuery.getResultList();

            if (usersWithUsername.size() == 0) {
                LOGGER.info("User does not exist with this username");
                em.getTransaction().commit();
                em.close();
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            // assuming there's only one user with this username
            User user = usersWithUsername.get(0);

            if (! user.getPassword().equals(userDTO.getPassword())) {
                LOGGER.info("User exists with this username, but password is incorrect");
                em.getTransaction().commit();
                em.close();
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);

            }

            // Generate token for user
            LOGGER.info("User exists with this username and password. Generating token for this user (and persisting this information), and passing this back in HTTP response");
            token = LoginResource.generateToken();

            // Store token in user object, and update user object in database (i.e. 
            // so the information that "this user has this cookie" is persisted)
            user.setToken(token);
            em.merge(user);

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        // Put token in Cookie, that will be returned
        NewCookie aNewCookie = new NewCookie(Config.CLIENT_COOKIE_NAME, token);

        return Response.status(Response.Status.OK).cookie(aNewCookie).build();
    }

    public static String generateToken() {
        // Generate token, feel free to change this to whatever you want,
        // should be unique. Also, we've chosen to go with a string,
        // because ?Cookies can store a string easily?
        String token = UUID.randomUUID().toString();

        return token; 
    }

    /**
     * A cookie is passed to this method containing a token value (expected to be made
     * from the above); the webserver checks its storage for the user that this token
     * value belongs to: this authenticates the client as that user.
     * 
     * The philosophy of token-based authentication being "no one other than the user 
     * (which owns that token value) can know that token value, so it must be that user".
     * Of course, this can be flawed, but we're happy with that.
     * 
     * If there isn't a user for this token value or there was no token value given 
     * (i.e. there's no one to authenticate), i.e. the webserver is bad at storing 
     * token values or the user sent a gibberish token value: 
     * throws Response.Status.Unauthorized (more like "unauthenticated")
     */
    public static User authenticateFromCookie(Cookie aCookie) {
        LOGGER.info("Authenticating from a cookie...");

        if (aCookie == null) {
            LOGGER.info("Cookie is null, so can't authenticate via cookie; please login via '../login' to get a cookie that has a token, that can be used to authenticate you. Throwing Response.Status.UNAUTHORIZED"); // if they've logged in, they should have a Cookie with name Config.COOKIE_NAME
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        // prep info about cookie
        String cookieValue = aCookie.getValue(); // aka token value (cookie as a means to store token value)
        String token = cookieValue; // for clarity

        // get users with that token from database
        EntityManager em = PersistenceManager.instance().createEntityManager();

        List<User> usersWithThatToken;

        try {
            em.getTransaction().begin();

            String queryString = String.format("select u from User u where u.token = '%s'", token);
            TypedQuery<User> tokenQuery = em.createQuery(queryString, User.class).setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            usersWithThatToken = tokenQuery.getResultList();

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        if (usersWithThatToken.size() == 0) {
            LOGGER.info("Webserver does not recognise token, i.e. token is not 'registered' to user. Throwing Response.Status.UNAUTHORIZED");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED); // not authenticated, because webserver doesn't recognise token (.e. doesn't recognize token as being registered to any user). I don't think the tests test this case.
         }
         
        // assuming that if there's a token with this value, there's only one user 
        // with this token value (otherwise our means of generating tokens is bad,
        // because generating a token should be unique)
        User authenticatedUser = usersWithThatToken.get(0);
        LOGGER.info(String.format("User with username '%s' has been authenticated!", authenticatedUser.getUsername()));

        return authenticatedUser;
    }





}
