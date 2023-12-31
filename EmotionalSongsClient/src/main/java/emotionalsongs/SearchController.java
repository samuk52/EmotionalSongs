package emotionalsongs;

/*
 * Progetto svolto da:
 *
 * Corallo Samuele 749719, Ateneo di Varese
 * Della Chiesa Mattia 749904, Ateneo di Varese
 *
 */

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * The Controller class that manages the search of songs in the database.
 * <p>
 *     The classe manages the {@link Node} that is displayed when the user clicks the "searchBtn" button
 *     in the main view (managed by {@link EmotionalSongsClientController}).
 * </p>
 *
 * @author <a href="https://github.com/samuk52">Corallo Samuele</a>
 */
public class SearchController implements Initializable {

    @FXML private VBox pane;
    @FXML private TextField searchField;
    @FXML private TextField yearField;
    @FXML private Button searchBtn;
    @FXML private Button removeSearchBtn;
    @FXML private Button advancedSearchBtn;
    @FXML private Button titleSearchBtn;
    @FXML private Button authorAndYearSearchBtn;
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane gridPane;
    @FXML private ImageView removeSearchImg;
    @FXML private Label infoLabel;
    @FXML private HBox advancedSearchBox;

    private GUIUtilities guiUtilities;
    private boolean advancedSearchActivated;
    private String filteredSearch;
    private static List<Node> songsPane;

    /**
     * Initializes the {@link Node} for song search.
     * <p>
     *     This method is called automatically when the JavaFX dialog associated with this controller is loaded.
     *     It initializes various UI components and sets up their behavior.
     * </p>
     *
     * @param url The URL of the FXML file.
     * @param resourceBundle The ResourceBundle associated with the FXML file.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // setto l'auto resize dei componenti/elementi che si trovano nello scroll pane
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        // create a new guiUtilities object with pattern singleton
        guiUtilities = GUIUtilities.getInstance();

        // initially the advanced search is not activated
        advancedSearchActivated = false;

        // initialize the songsPane list
        songsPane = new ArrayList<>();

        // initially the search is set to titleSearch
        handleTitleSearchButtonAction();

        // aggiungo come vincolo alla yearField l'inserimento di soli numeri e di massimo 4 numeri
        GUIUtilities.forceNumericInput(yearField, 4);

        // add a listener to searchField, which displays removeSearchBtn.
        searchField.textProperty().addListener((observableValue, s, t1) -> {
            // se la searchField non è vuota, visualizzo e rendo premibile il pulsante di elimina ricerca
            if(filteredSearch.equalsIgnoreCase("title")) {
                if (!searchField.getText().isEmpty()) {
                    showRemoveSearchBtn();
                } else {
                    /*
                    altrimenti se la text field è vuota rendo non visibile e non premibile il pulsante
                    di elimina ricerca
                    */
                    hideRemoveSearchBtn();
                }
            }else{
                // add a listener to yearField
                yearField.textProperty().addListener((observableValue1, s1, t11) -> {
                    /*
                    se la searchFiled e la yearField non sono vuote, visualizzo e rendo premibile
                    il pulsante di elimina ricerca
                    */
                    if (!searchField.getText().isEmpty() && !yearField.getText().isEmpty()) {
                        showRemoveSearchBtn();
                    } else {
                        /*
                        altrimenti se solo una della due textField è vuota rendo non visibile e non premibile
                        il di elimina ricerca
                         */
                        hideRemoveSearchBtn();
                    }
                });
                /*
                 se la searchFiled e la yearField non sono vuote, visualizzo e rendo premibile
                 il pulsante di elimina ricerca
                 */
                if (!searchField.getText().isEmpty() && !yearField.getText().isEmpty()) {
                    showRemoveSearchBtn();
                } else {
                    /*
                    altrimenti se solo una della due textField è vuota rendo non visibile e non premibile
                    il di elimina ricerca
                   */
                    hideRemoveSearchBtn();
                }
            }
        });
    }

    /**
     * Method that manages the behaviour of the song search operation.
     * <p>
     *     When the enter key on the keyboard is clicked, depending on the type of search (by title or by author and year),
     *     the songs found are displayed; if no song is found, a message is displayed informing the user of this.
     * </p>
     * @param key The {@link KeyEvent} activated by pressing the enter key on the keyboard.
     */
    @FXML
    public void cercaBranoMusicale(KeyEvent key){

        try {

            // la ricerca viene effettuata quando viene premuto il pulsante di INVIO
            if (key.getCode() == KeyCode.ENTER) {

                // remove the last search before the new search
                removeLastSearch();

                Set<Canzone> songs;

                if (filteredSearch.equalsIgnoreCase("title")) {

                    if(searchField.getText().isEmpty()){

                        return;
                    }

                    songs = EmotionalSongs.repo.ricercaCanzone(searchField.getText());

                } else {

                    if(searchField.getText().isEmpty() || yearField.getText().isEmpty()){

                        return;
                    }

                    songs = EmotionalSongs.repo.ricercaCanzone(searchField.getText(), yearField.getText());

                }

                // verifico se le canzoni restituite non sono vuote
                if (!songs.isEmpty()) {
                    // visualizzo le canzoni restituite dal db

                    // set the content of scroll pane
                    scrollPane.setContent(gridPane);

                    // visualizzo le canzoni restituite dal db
                    int row = 0;
                    for (Canzone song : songs) {
                        setNewSongFound(song, row);
                        row ++;
                    }
                } else {

                    try{
                        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("noSearchResult.fxml"));
                        Node noSearchResult_pane = fxmlLoader.load();

                        // add the noSearchResult_pane to scroll pane.
                        scrollPane.setContent(noSearchResult_pane);

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

        }catch (RemoteException e){

            Stage connectionFailedStage = new Stage();

            connectionFailedStage.setScene(GUIUtilities.getInstance().getScene("connectionFailed.fxml"));
            connectionFailedStage.initStyle(StageStyle.UNDECORATED);
            connectionFailedStage.initModality(Modality.APPLICATION_MODAL);
            connectionFailedStage.setResizable(false);
            connectionFailedStage.show();
        }
    }

    /**
     * Method that handles the behaviour of the {@link SearchController#removeSearchBtn}.
     * <p>
     *     When the {@link SearchController#removeSearchBtn} is clicked, the search is reset.
     * </p>
     */
    @FXML
    public void handleRemoveSearchButtonAction(){

        // nascondo il bottone di rimuovi ricerca
        hideRemoveSearchBtn();

        // set the content of scroll pane
        scrollPane.setContent(gridPane);

        // controllo il filtro della ricerca
        if(filteredSearch.equalsIgnoreCase("title")) {
            // reset the searchField
            searchField.setText("");
        }else{
            // reset the searchField and the yearField
            searchField.setText("");
            yearField.setText("");
        }

        // remove last search (reset the grid pane and the text filed)
        removeLastSearch();
    }

    /**
     * Method that handles the behaviour of the {@link SearchController#advancedSearchBtn}.
     *
     *     When the {@link SearchController#advancedSearchBtn} is clicked, its behavior varies based on whether:
     *     <ol>
     *         <li>
     *             the {@link SearchController#advancedSearchBox} is not visible, in this case, it is made visible
     *             and with it the buttons are also made visible: 'titleSearchBtn' and 'authorAndYearSearchBtn'
     *             which will allow filtering the search.
     *         </li>
     *         <li>
     *             the {@link SearchController#advancedSearchBox} is visible, in this case when the 'AdvancedSearchBtn' button is clicked,
     *             the advanced search box is made invisible.
     *         </li>
     *     </ol>
     *
     */
    public void handleAdvancedSearchButtonAction(){
        // verifico se la ricerca avanzata è attivata o meno
        if(!advancedSearchActivated){ // se non è attivata
            advancedSearchBox.setVisible(true);
            advancedSearchActivated = true;
        }else{ // se è attivata
            advancedSearchBox.setVisible(false);
            advancedSearchActivated = false;
        }
    }

    /**
     * Method that handles the behaviour of the {@link SearchController#titleSearchBtn}.
     * <p>
     *     When the {@link SearchController#titleSearchBtn} is clicked, the search is set to: search by title.
     * </p>
     */
    @FXML
    public void handleTitleSearchButtonAction(){
        // set the searchField prompt text
        searchField.setPromptText("Inserisci il titolo della canzone");

        // make the yearField not visibile and disable
        yearField.setDisable(true);
        yearField.setVisible(false);

        // change style of searchFilterButtons
        guiUtilities.setNodeStyle(titleSearchBtn, "searchFilterButton", "searchFilterButtonClicked");
        guiUtilities.setNodeStyle(authorAndYearSearchBtn, "searchFilterButtonClicked", "searchFilterButton");

        // set the filtered
        filteredSearch = "title";

        // remove the last search
        handleRemoveSearchButtonAction();
    }

    /**
     * Method that handles the behaviour of the {@link SearchController#authorAndYearSearchBtn}.
     * <p>
     *     When the {@link SearchController#authorAndYearSearchBtn} is clicked,
     *     the search is set to: search by author and year.
     * </p>
     */
    public void handleAuthorAndYearSearchButtonAction(){
        // set the searchField prompt text
        searchField.setPromptText("Inserisci l'autore della canzone");

        // make the yearField not disable and visible
        yearField.setDisable(false);
        yearField.setVisible(true);

        // change style of searchFilterButtons
        guiUtilities.setNodeStyle(authorAndYearSearchBtn, "searchFilterButton", "searchFilterButtonClicked");
        guiUtilities.setNodeStyle(titleSearchBtn, "searchFilterButtonClicked", "searchFilterButton");

        // set the filtered
        filteredSearch = "authorAndYear";

        // remove the last search
        handleRemoveSearchButtonAction();
    }

    /**
     * Method that adds the song passed as a parameter to the {@link SearchController#gridPane}.
     *
     * @param song represents the song to add to the {@link SearchController#gridPane}.
     * @param row indicates the row of the {@link SearchController#gridPane} in which to insert the song.
     */
    private void setNewSongFound(Canzone song, int row){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("song.fxml"));
            Node song_pane = fxmlLoader.load();

            // get song controller, this it serves for set song name and author name of the song
            SongController songController = fxmlLoader.getController();
            songController.setData(song);

            // add the song pane loded to songsPane list
            songsPane.add(song_pane);

            // add the fxml song view in the gridPane
            gridPane.add(song_pane, 0, row);
            // set the margin between songs (the space between songs)
            GridPane.setMargin(song_pane, new Insets(10));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that restores the {@link SearchController#gridPane}, removing from it the songs
     * found since the last search.
     */
    private void removeLastSearch(){
        /*
        metodo che "rimuove" l'ultima ricerca, ovvero va a resettare il gridpane
         */
        for(Node pane : songsPane){
            gridPane.getChildren().remove(pane);
        }

        /*
         clear list that contains panes of last search ("pulisco" la lista che contiene i
         panes dell'ultima ricerca, questo viene fatto per evitare che troppo stazio venga occupato)
         */
        songsPane.clear();
    }

    /**
     * Method that changes the display of the {@link SearchController#removeSearchBtn}.
     * <p>
     *    When invoked, the {@link SearchController#removeSearchBtn} is made active, visible and its image is changed.
     * </p>
     */
    public void showRemoveSearchBtn(){
        removeSearchBtn.setDisable(false);
        removeSearchImg.setImage(guiUtilities.getImage("removeSearch"));
        infoLabel.setVisible(true);
    }

    /**
     * Method that changes the display of the {@link SearchController#removeSearchBtn}.
     * <p>
     *     When invoked, the {@link SearchController#removeSearchBtn} is disabled, made not visible and its image is changed.
     * </p>
     */
    public void hideRemoveSearchBtn(){
        removeSearchBtn.setDisable(true);
        removeSearchImg.setImage(guiUtilities.getImage("search"));
        infoLabel.setVisible(false);
    }
}
