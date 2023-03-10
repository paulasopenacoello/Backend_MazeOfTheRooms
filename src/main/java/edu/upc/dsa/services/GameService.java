
package edu.upc.dsa.services;

import edu.upc.dsa.GameManager;
import edu.upc.dsa.GameManagerDBImpl;

import edu.upc.dsa.exceptions.*;
import edu.upc.dsa.models.*;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Api(value = "/", description = "Endpoint to Track Service")
@Path("/shop")
public class GameService {

    private GameManager tm;
    final static org.apache.log4j.Logger logger = Logger.getLogger(GameManagerDBImpl.class);

    public GameService() throws EmailAlreadyBeingUsedException, SQLException, GadgetWithSameIdAlreadyExists, FAQAlreadyBeingAskedException, FileNotFoundException {
        this.tm = GameManagerDBImpl.getInstance();
        logger.info("Hey im here using the service");

        if (tm.numUsers()==0) {
            this.tm.addUser("Alba", "Roma", "23112001", "albaroma@gmail.com", "123456","https://i.pinimg.com/236x/56/77/62/5677627c338956d1cb9bbdb7f49ae79e.jpg");
            this.tm.addUser("Maria", "Ubiergo", "02112001", "meri@gmail.com", "123456", "https://i.pinimg.com/236x/e9/57/2a/e9572a70726980ed5445c02e1058760b.jpg");
            String idUser = this.tm.addUser("Guillem", "Purti", "02112001", "guille@gmail.com", "123456", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTty5Z4hEeNEUICuhCAREChxEOhLSAL3KthnN9Cul7zs_gmb73Gcjz09LMFcC-R1q8d2Zc&usqp=CAU");
            this.tm.upgradeUserToAdmin(idUser);        }
        if(tm.numGadgets()==0) {
            this.tm.addGadget("1",3,"Water Retaw","https://art.pixilart.com/sr2d6755ba5061f.png");
            this.tm.addGadget("2",8,"Fire Erif","https://art.pixilart.com/sr2b28f1e9e62aa.png");
            this.tm.addGadget("3",550,"Earth Htrae","https://art.pixilart.com/sr22f5a76499f92.png");
            this.tm.addGadget("4",2,"Cloud Duolc","https://art.pixilart.com/sr23f2f07caa6d8.png");
        }
        if(tm.numFAQs()==0){
            this.tm.addFAQ("Where can I see my gadgets?", "Go to your profile section");
            this.tm.addFAQ("Can I use this app in an iphone?", "This app is not available in iOS, yet!");
            this.tm.addFAQ("Who are you?", "The developers are: Paula, Alba, Genis, Guillem and Maria");
            this.tm.addFAQ("How do I get experience?", "Playing more and more");
        }
    }

    @GET
    @ApiOperation(value = "Gives the shop gadgets", notes = "ordered by price")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = Gadget.class, responseContainer="List")
    })
    @Path("/gadget/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGadgets() {

        List<Gadget> gadgetList = this.tm.gadgetList();
        GenericEntity<List<Gadget>> entity = new GenericEntity<List<Gadget>>(gadgetList) {};
        return Response.status(201).entity(entity).build();
    }

    @GET
    @ApiOperation(value = "Gives the users", notes = "User list")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = User.class, responseContainer="List")
    })
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        logger.info("Arrived to the service");
        List<User> listUsers= new ArrayList<>(this.tm.getUsers().values());
        GenericEntity<List<User>> entity = new GenericEntity<List<User>>(listUsers) {};
        return Response.status(201).entity(entity).build();
    }

    @GET
    @ApiOperation(value = "Gives the FAQs", notes = "")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = FAQ.class, responseContainer="List")
    })
    @Path("/FAQs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFAQs() {

        List<FAQ> FAQsList = this.tm.FAQsList();
        GenericEntity<List<FAQ>> entity = new GenericEntity<List<FAQ>>(FAQsList) {};
        return Response.status(201).entity(entity).build();
    }

    @GET
    @ApiOperation(value = "Gives a Gadget by id", notes = "With an id")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = Gadget.class),
            @ApiResponse(code = 404, message = "Gadget does not exist")
    })
    @Path("/gadget/{idGadget}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGadget(@PathParam("idGadget") String id) {
        try {
            Gadget gadget = (Gadget) this.tm.getGadget(id);
            return Response.status(201).entity(gadget).build();
        }
        catch (GadgetDoesNotExistException E){
            return Response.status(404).build();
        }
    }

    @GET
    @ApiOperation(value = "Gives a User by id", notes = "With an id")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = UserInformation.class),
            @ApiResponse(code = 404, message = "User does not exist")
    })
    @Path("/user/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("idUser") String id) {
        try {
            User user = this.tm.getUser(id);
            //UserInformation info = new UserInformation(user.getName(), user.getSurname(), user.getBirthday(), user.getEmail(), user.getPassword());
            UserInformation info = new UserInformation(user);
            return Response.status(201).entity(info).build();
        }
        catch (UserDoesNotExistException E){
            return Response.status(404).build();
        }
    }

    @POST
    @ApiOperation(value = "create a new User", notes = "Do you want to register to our shop?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response= UserInformation.class),
            @ApiResponse(code = 409, message = "This user already exists."),
            @ApiResponse(code = 500, message = "Empty credentials")
    })
    @Path("/user/register")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response newUser(UserInformation newUser){
        if (Objects.equals(newUser.getName(), "") || Objects.equals(newUser.getBirthday(), "") || Objects.equals(newUser.getEmail(), "") || Objects.equals(newUser.getPassword(), "") || Objects.equals(newUser.getSurname(), ""))  return Response.status(500).entity(newUser).build();
        try{
            this.tm.addUser(newUser.getName(), newUser.getSurname(), newUser.getBirthday(), newUser.getEmail(), newUser.getPassword(), newUser.getProfilePicture());
            return Response.status(201).entity(newUser).build();
        }
        catch (EmailAlreadyBeingUsedException | SQLException E){
            return Response.status(409).entity(newUser).build();
        }


    }

    @POST
    @ApiOperation(value = "Login to the shop", notes = "Do you want to log in to our shop?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response= UserId.class),
            @ApiResponse(code = 409, message = "Wrong credentials.")
    })
    @Path("/user/login")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response logIn(Credentials credentials){
        try{
            logger.info(credentials.getEmail());
            String id = this.tm.userLogin(credentials);
            UserId idUser = new UserId(id);
            return Response.status(201).entity(idUser).build();
        }
        catch (IncorrectCredentialsException | SQLException E){
            return Response.status(409).build();
        }
    }

    @POST
    @ApiOperation(value = "create a new Gadget", notes = "Do you want to create a new Gadget?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response= Gadget.class),
            @ApiResponse(code = 500, message = "Some parameter is null or not valid")
    })
    @Path("/gadget/create")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response newGadget(Gadget newGadget){
        if (newGadget.getIdGadget()==null || newGadget.getCost()<0 || newGadget.getDescription()==null || newGadget.getUnityShape()==null)  return Response.status(500).entity(newGadget).build();
        try {
            this.tm.addGadget(newGadget.getIdGadget(),newGadget.getCost(),newGadget.getDescription(),newGadget.getUnityShape());
        } catch (SQLException | GadgetWithSameIdAlreadyExists e) {
            throw new RuntimeException(e);
        }
        return Response.status(201).entity(newGadget).build();
    }

    @PUT
    @ApiOperation(value = "buy a Gadget", notes = "Do you want to buy a Gadget?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 409, message = "Wrong id."),
            @ApiResponse(code = 401, message = "Gadget does not exist"),
            @ApiResponse(code = 403, message = "You have not enough money ")
    })
    @Path("/gadget/buy/{idGadget}/{idUser}")
    public Response buyAGadget(@PathParam("idGadget")String idGadget,@PathParam("idUser") String idUser) {

        try{
            this.tm.buyGadget(idGadget,idUser);
            return Response.status(201).build();
        }
        catch (NotEnoughMoneyException e){
            return Response.status(403).build();
        }
        catch (GadgetDoesNotExistException e) {
            return Response.status(401).build();
        }
        catch (UserDoesNotExistException | SQLException e) {
            return Response.status(409).build();
        }
    }

    @PUT
    @ApiOperation(value = "update a Gadget", notes = "Do you want to update a Gadget?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 401, message = "Gadget does not exist")
    })
    @Path("/gadget/update")
    public Response updateAGadget(Gadget gadget) {
        try{
            this.tm.updateGadget(gadget);
            return Response.status(201).build();
        }
        catch (GadgetDoesNotExistException | SQLException e) {
            return Response.status(401).build();
        }
    }

    @DELETE
    @ApiOperation(value = "Deletes a gadget", notes = "Deletes a gadget")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 404, message = "Gadget not found")
    })
    @Path("/gadget/delete/{idGadget}")
    public Response deleteGadget(@PathParam("idGadget") String id) {
        try{
            this.tm.deleteGadget(id);
            return Response.status(201).build();
        }
        catch (GadgetDoesNotExistException e) {
            return Response.status(401).build();
        }
    }

    @GET
    @ApiOperation(value = "Gives the purchases of a user")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = String.class, responseContainer="List"),
            @ApiResponse(code = 404, message = "No purchase found for that user"),
            @ApiResponse(code = 500, message = "Error in the databases")
    })
    @Path("/purchase/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response purchasedGadgets(@PathParam("idUser") String idUser) {
        logger.info("Seeing the purchased gadgets by a user");
        try {
            List<Gadget> listGadgets= this.tm.purchasedGadgets(idUser);
            GenericEntity<List<Gadget>> entity = new GenericEntity<List<Gadget>>(listGadgets) {};
            return Response.status(201).entity(entity).build();
        } catch (SQLException e) {
            return Response.status(500).build();
        } catch (NoPurchaseWasFoundForIdUser e) {
            return Response.status(404).build();
        } catch (GadgetDoesNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    @PUT
    @ApiOperation(value = "update Password", notes = "Do you want to change the password?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful password change"),
            @ApiResponse(code = 403, message = "Incorrect credentials exception."),
            @ApiResponse(code = 404, message = "Not found the user in DB")
    })
    @Path("/user/update")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateUserPassword(PasswordChangeRequirements passwordChangeRequirements){
        try{
            this.tm.updateUserPassword(passwordChangeRequirements);
            return Response.status(201).build();
        }
        catch (SQLException E){
            return Response.status(404).build();
        } catch (IncorrectCredentialsException e) {
            return Response.status(403).build();
        }
    }

    @GET
    @ApiOperation(value = "Gives the ranking of users", notes = "User list")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = User.class, responseContainer="List"),
            @ApiResponse(code = 404, message = "Not users found in DB"),
    })
    @Path("/user/allOrdered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rankingOfUsers(){
        try{
            logger.info("Ranking of Users being cooked...");
            List<User> rankingOfUsers = this.tm.rankingOfUsers();
            GenericEntity<List<User>> entity = new GenericEntity<List<User>>(rankingOfUsers) {};
            return Response.status(201).entity(entity).build();
        }
        catch(SQLException E){
            return Response.status(201).build();
        }
    }

    @PUT
    @ApiOperation(value = "Deletes a purchased gadget", notes = "Deletes a purchased gadget")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful")
    })
    @Path("/user/deletePurchase")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deletePurchasedGadget(Purchase purchase) {
        this.tm.deletePurchasedGadget(purchase);
        return Response.status(201).build();

    }

    @POST
    @ApiOperation(value = "create a new User", notes = "Do you want to register to our shop?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 409, message = "Something bad happened"),
            @ApiResponse(code = 500, message = "Empty name")
    })
    @Path("/user/chat/newMessage")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response newChatMessage(ChatMessage chatMessage){
        if (Objects.equals(chatMessage.getName(), ""))
            return Response.status(500).build();
        try{
            this.tm.postChatMessage(chatMessage);
            return Response.status(201).build();
        }
        catch(SQLException E){
            return Response.status(409).build();
        }

    }

    @GET
    @ApiOperation(value = "See all the chat", notes = "of all your friends")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = ChatMessage.class, responseContainer="List")
    })
    @Path("/user/chat/{num}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChat(@PathParam("num") int num) {
        List<ChatMessage> listOfChats = this.tm.getChat(num);
        GenericEntity<List<ChatMessage>> entity = new GenericEntity<List<ChatMessage>>(listOfChats) {};
        return Response.status(201).entity(entity).build();
    }

    @POST
    @ApiOperation(value = "post an abuse", notes = "report you abuses")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 403, message = "DB problems"),
            @ApiResponse(code = 500, message = "Put your name, informer")
    })
    @Path("/issue")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response postAbuse(Abuse newAbuse){
        if (newAbuse.getInformer()==null)  return Response.status(500).build();
        try {
            this.tm.reportAbuse(newAbuse);
            return Response.status(201).build();
        } catch (SQLException e) {
            return Response.status(403).build();
        }

    }

    @POST
    @ApiOperation(value = "add a question", notes = "Do you want to add a question?")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful")

    })
    @Path("/user/question")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response newQuestion(Question question) throws SQLException {
        this.tm.addQuestion(question);
        return Response.status(201).entity(question).build();
    }

    @POST
    @ApiOperation(value = "Save game info", notes = "Get experience from the game")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful")

    })
    @Path("/user/saveGame")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response saveGame(GameInfo gameInfo) throws SQLException, UserDoesNotExistException {
        this.tm.saveGame(gameInfo);
        return Response.status(201).entity(gameInfo).build();
    }

    @GET
    @ApiOperation(value = "See all the chat", notes = "of all your friends")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful", response = GadgetName.class, responseContainer="List")
    })
    @Path("/user/loadGame/{idUser}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadGame(@PathParam("idUser") String idUser) throws SQLException, NoPurchaseWasFoundForIdUser, GadgetDoesNotExistException {
        List<GadgetName> startGameInfo = this.tm.loadGame(idUser);
        GenericEntity<List<GadgetName>> entity = new GenericEntity<List<GadgetName>>(startGameInfo) {};
        return Response.status(201).entity(entity).build();
    }

    @PUT
    @ApiOperation(value = "Update of the profile picture", notes = "profile picture")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successful"),
            @ApiResponse(code = 404, message = "Drama")

    })
    @Path("/user/update/profilePicture")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateProfilePicture(ProfilePicture profilePicture){
        try{
            this.tm.updateProfilePicture(profilePicture);
            return Response.status(201).build();
        }
        catch (SQLException e) {
            return Response.status(404).build();
        }
    }
}


