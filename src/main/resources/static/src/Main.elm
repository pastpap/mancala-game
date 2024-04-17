module Main exposing (..)

import Array exposing (Array, length)
import Browser
import Element exposing (..)
import Element.Background as Background
import Element.Border as Border
import Element.Events
import Element.Font as Font
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http exposing (Error(..))
import Json.Decode as JsonDecode exposing (Decoder, bool, field, int, string)
import Json.Encode as JsonEncode
import String



-- MAIN


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type Model
    = Failure String
    | Loading
    | Success GameState Bool


type alias GameState =
    { board : Array Int
    , turnPlayer : Int
    , finished : Bool
    , winner : Int
    , winnerScore : Int
    , message : String
    }


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getFreshBoard )



-- UPDATE


type Msg
    = MorePlease
    | PlayMove Int GameState
    | GotGame (Result Http.Error GameState)
    | ExpandRules GameState
    | HideRules GameState


update : Msg -> Model -> ( Model, Cmd Msg )
update msg _ =
    case msg of
        MorePlease ->
            ( Loading, getFreshBoard )

        PlayMove moveIndex gameState ->
            ( Loading, postNewMove moveIndex gameState )

        GotGame result ->
            case result of
                Ok gameState ->
                    ( Success gameState True, Cmd.none )

                Err err ->
                    ( Failure (errorToString err), Cmd.none )

        ExpandRules gameState ->
            ( Success gameState False, Cmd.none )

        HideRules gameState ->
            ( Success gameState True, Cmd.none )


errorToString : Http.Error -> String
errorToString error =
    case error of
        BadUrl url ->
            "The URL " ++ url ++ " was invalid"

        Timeout ->
            "Unable to reach the server, try again"

        NetworkError ->
            "Unable to reach the server, check your network connection"

        BadStatus 500 ->
            "The server had a problem, try again later"

        BadStatus 400 ->
            "Verify your information and try again"

        BadStatus _ ->
            "Unknown error"

        BadBody errorMessage ->
            errorMessage



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


stringFromBool : Bool -> String
stringFromBool value =
    if value then
        "True"

    else
        "False"


view : Model -> Html Msg
view model =
    Element.layout []
        (mainPageColumn model)


mainPageColumn : Model -> Element Msg
mainPageColumn model =
    case model of
        Failure errorString ->
            column
                [ Element.width fill
                , Element.height fill
                , spacing 5
                , Background.color primaryColor
                ]
                [ row
                    [ Element.width fill
                    , paddingXY 20 10
                    , Background.color primaryColor
                    , Font.color white
                    , Border.widthXY 0 1
                    , Border.color secondaryColor
                    ]
                    [ el
                        [ Font.bold ]
                        (Element.text "Mancala - The board game")
                    , el
                        [ alignRight
                        , Font.color orange
                        , Font.bold
                        ]
                        (Element.text errorString)
                    ]
                , sidebar
                ]

        Loading ->
            column
                [ Element.width fill
                , Element.height fill
                , spacing 5
                , Background.color primaryColor
                ]
                [ row
                    [ Element.width fill
                    , paddingXY 20 10
                    , Background.color primaryColor
                    , Font.color white
                    , Border.widthXY 0 1
                    , Border.color secondaryColor
                    ]
                    [ el
                        [ Font.bold, centerX ]
                        (Element.text "Loading...")
                    ]
                ]

        Success gameState isHiddenRules ->
            column
                [ Element.width fill
                , Element.height fill
                , spacing 5
                , Background.color secondaryColor
                ]
                [ header gameState
                , contentRow gameState isHiddenRules
                ]


header : GameState -> Element Msg
header gameState =
    row
        [ Element.width fill
        , paddingXY 20 10
        , Background.color primaryColor
        , Font.color white
        , Border.widthXY 0 1
        , Border.color secondaryColor
        ]
        [ el
            [ Font.bold ]
            (Element.text "Mancala - The board game")
        , el
            [ centerX
            , Font.color red
            , Font.bold
            ]
            (Element.text gameState.message)
        , el
            [ alignRight
            , Font.color lightBlue
            , Font.bold
            ]
            (Element.text ("Turn: " ++ displayCurrentUser gameState.turnPlayer))
        ]


showMessageIfWin : GameState -> String
showMessageIfWin gameState =
    if gameState.winnerScore == 0 then
        ""

    else
        gameState.message


displayCurrentUser : Int -> String
displayCurrentUser userCode =
    if userCode == 0 then
        "Player One"

    else
        "Player Two"


contentRow : GameState -> Bool -> Element Msg
contentRow gameState isHiddenRules =
    row [ Element.height fill, Element.width fill ]
        [ sidebar
        , content gameState isHiddenRules
        ]


createRefreshButton : String -> Element Msg
createRefreshButton textValue =
    el
        [ centerX
        , Border.width 1
        , Border.rounded 5
        , paddingXY 3 3
        , Background.color (rgb255 112 112 112)
        , mouseOver [ Font.color orange ]
        , Element.Events.onClick MorePlease
        , pointer
        ]
        (Element.text textValue)


sidebar : Element Msg
sidebar =
    column
        [ Element.height fill
        , paddingXY 15 10
        , spacing 15
        , Font.size 20
        , Font.color white
        , Background.color thirdColor
        ]
        [ createRefreshButton "Reset"
        ]


createExpandButton : GameState -> Bool -> Element Msg
createExpandButton gameState isHiddenRules =
    if isHiddenRules then
        el
            [ alignLeft
            , Element.width
                (fill
                    |> maximum 40
                    |> minimum 40
                )
            , Border.width 1
            , Border.rounded 5
            , paddingXY 3 3
            , Background.color (rgb255 112 112 112)
            , Font.center
            , mouseOver [ Font.color orange ]
            , Element.Events.onClick (ExpandRules gameState)
            , pointer
            ]
            (Element.text "+")

    else
        column [ Element.width fill ]
            [ el
                [ alignLeft
                , Element.width
                    (fill
                        |> maximum 40
                        |> minimum 40
                    )
                , Border.width 1
                , Border.rounded 5
                , paddingXY 3 3
                , Background.color (rgb255 112 112 112)
                , Font.center
                , mouseOver [ Font.color orange ]
                , Element.Events.onClick (HideRules gameState)
                , pointer
                ]
                (Element.text "-")
            , Element.column
                [ Element.width
                    (fill
                        |> maximum 1280
                    )
                , Element.padding 20
                ]
                [ paragraph
                    [ Font.color white
                    , Font.bold
                    , Font.size 30
                    ]
                    [ Element.text
                        "Mancala Game Rules"
                    ]
                , paragraph [ alignLeft, paddingXY 0 10 ] []
                , paragraph
                    [ Font.color white
                    ]
                    [ Element.text
                        "Each of the two players has his six pits in front of him. To the right of the six pits, each player has a larger pit. At the start of the game, there are six stones in each of the six round pits ."
                    ]
                , paragraph [ alignLeft, paddingXY 0 10 ] []
                , paragraph [ alignLeft, Font.color white, Font.bold ]
                    [ Element.text "Gameplay"
                    ]
                , paragraph
                    [ Font.color white ]
                    [ Element.text
                        "The player who begins with the first move picks up all the stones in any of his own six pits, and sows the stones on to the right, one in each of the following pits, including his own big pit. No stones are put in the opponents' big pit. If the player's last stone lands in his own big pit, he gets another turn. This can be repeated several times before it's the other player's turn."
                    ]
                , paragraph [ alignLeft, paddingXY 0 10 ] []
                , paragraph [ alignLeft, Font.color white, Font.bold ]
                    [ Element.text "Capturing stones"
                    ]
                , paragraph
                    [ Font.color white
                    ]
                    [ Element.text
                        "During the game the pits are emptied on both sides. Always when the last stone lands in an own empty pit, the player captures his own stone and all stones in the opposite pit (the other player’s pit) and puts them in his own (big or little?) pit."
                    ]
                , paragraph [ alignLeft, paddingXY 0 10 ] []
                , paragraph [ alignLeft, Font.color white, Font.bold ]
                    [ Element.text "The Game Ends"
                    ]
                , paragraph
                    [ Font.color white
                    ]
                    [ Element.text
                        "The game is over as soon as one of the sides runs out of stones. The player who still has stones in his pits keeps them and puts them in his big pit. The winner of the game is the player who has the most stones in his big pit."
                    ]
                ]
            ]


content : GameState -> Bool -> Element Msg
content gameState isHiddenRules =
    column
        [ Element.width fill
        , Element.height fill
        ]
        [ row
            [ Element.width fill
            ]
            [ column
                [ alignTop
                , centerX
                , Element.width (fill |> maximum 1024)
                , Element.height
                    (fill
                        |> maximum 300
                        |> minimum 300
                    )
                ]
                [ el
                    [ alignLeft
                    , Font.color white
                    , paddingXY 0 10
                    ]
                    (Element.text "Player Two")
                , row
                    [ Element.width fill
                    , Element.height
                        (fill
                            |> maximum 300
                        )
                    , Border.width 5
                    , Border.color white
                    ]
                    [ playerTwoMancala gameState
                    , centerPits gameState
                    , playerOneMancala gameState
                    ]
                , el
                    [ alignRight
                    , Font.color white
                    , paddingXY 0 10
                    ]
                    (Element.text "Player One")
                ]
            ]
        , row [ Element.width fill ]
            [ el
                [ Element.width fill
                , paddingXY 20 20
                ]
                (createExpandButton gameState isHiddenRules)
            ]
        ]


playerTwoMancala : GameState -> Element Msg
playerTwoMancala gameState =
    column
        [ Element.height fill
        , Element.width (fillPortion 2)
        , Border.width 2
        , Border.color white
        , Font.color white
        ]
        [ el [ centerX, centerY ] (Element.text (maybeIntToString (Array.get (length gameState.board - 1) gameState.board))) ]


maybeIntToString : Maybe Int -> String
maybeIntToString maybeInt =
    case maybeInt of
        Nothing ->
            ""

        Just value ->
            String.fromInt value


centerPits : GameState -> Element Msg
centerPits gameState =
    column
        [ Element.height fill
        , Element.width (fillPortion 6)
        , Border.color white
        ]
        [ renderBoardPlayerTwo gameState
        , renderBoardPlayerOne gameState
        ]


playerOneMancala : GameState -> Element Msg
playerOneMancala gameState =
    column
        [ Element.height fill
        , Element.width (fillPortion 2)
        , Border.width 2
        , Border.color white
        , Border.innerGlow secondaryColor 10
        , Font.color white
        ]
        [ el [ centerX, centerY ] (Element.text (maybeIntToString (Array.get (round (toFloat (length gameState.board) / 2) - 1) gameState.board))) ]


renderBoardCell : GameState -> ( Int, Int ) -> Element Msg
renderBoardCell gameState value =
    column
        [ Element.width fill
        , Element.height fill
        , Border.width 2
        , Border.color white
        , Border.rounded 75
        , Font.color white
        , mouseOver
            [ Background.color primaryColor
            ]
        , Element.Events.onClick (PlayMove (Tuple.first value) gameState)
        , pointer
        ]
        [ el
            [ centerX
            , centerY
            ]
            (Element.text (String.fromInt (Tuple.second value)))
        ]


renderBoardPlayerOne : GameState -> Element Msg
renderBoardPlayerOne gameState =
    row
        [ Element.width fill
        , Element.height fill
        ]
        (gameState.board
            |> Array.indexedMap (\i cell -> ( i, cell ))
            |> Array.filter (\( i, _ ) -> (i >= 0) && (i < round (toFloat (length gameState.board) / 2) - 1))
            |> Array.map (renderBoardCell gameState)
            |> Array.toList
        )


renderBoardPlayerTwo : GameState -> Element Msg
renderBoardPlayerTwo gameState =
    row
        [ Element.width fill
        , Element.height fill
        ]
        (gameState.board
            |> Array.indexedMap (\i cell -> ( i, cell ))
            |> Array.filter (\( i, _ ) -> (i < length gameState.board - 1) && (i >= round (toFloat (length gameState.board) / 2)))
            |> Array.map (renderBoardCell gameState)
            |> Array.toList
            |> List.reverse
        )



-- HTTP


getFreshBoard : Cmd Msg
getFreshBoard =
    Http.get
        { url = "http://localhost:8888/v1/reset"
        , expect = Http.expectJson GotGame payloadDecoder
        }


postNewMove : Int -> GameState -> Cmd Msg
postNewMove moveIndex gameState =
    Http.post
        { url = "http://localhost:8888/v1/play?position=" ++ String.fromInt moveIndex
        , body = Http.jsonBody (boardEncoder gameState)
        , expect = Http.expectJson GotGame payloadDecoder
        }



-- Decoders


payloadDecoder : Decoder GameState
payloadDecoder =
    JsonDecode.map6 GameState
        (field "board" boardDecoder)
        (field "turnPlayer" int)
        (field "finished" bool)
        (field "winningPlayer" int)
        (field "winningPlayerScore" int)
        (field "message" string)


boardDecoder : Decoder (Array Int)
boardDecoder =
    JsonDecode.array JsonDecode.int


boardEncoder : GameState -> JsonEncode.Value
boardEncoder gameState =
    JsonEncode.object
        [ ( "board", JsonEncode.array JsonEncode.int gameState.board )
        , ( "finished", JsonEncode.bool gameState.finished )
        , ( "turnPlayer", JsonEncode.int gameState.turnPlayer )
        , ( "winningPlayer", JsonEncode.int gameState.winner )
        , ( "winningPlayerScore", JsonEncode.int gameState.winnerScore )
        , ( "message", JsonEncode.string gameState.message )
        ]


primaryColor : Color
primaryColor =
    rgb255 3 10 17


secondaryColor : Color
secondaryColor =
    rgb255 1 53 51


thirdColor : Color
thirdColor =
    rgba255 0 46 46 78.0


orange : Color
orange =
    rgb255 198 125 52


red : Color
red =
    rgb255 255 5 5


lightBlue : Color
lightBlue =
    rgb255 154 142 237


gray : Color
gray =
    rgb255 172 221 216


darkGray : Color
darkGray =
    rgb255 112 112 112


white : Color
white =
    rgb255 255 255 255
