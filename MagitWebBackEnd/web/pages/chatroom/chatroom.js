var chatVersion = 0;
var refreshRate = 2000; //milli seconds
var USER_LIST_URL = buildUrlWithContextPath("userslist");
var CHAT_LIST_URL = buildUrlWithContextPath("chat");
var SEND_CHAT_URL = buildUrlWithContextPath("sendchat");
//users = a list of usernames, essentially an array of javascript strings:
// ["moshe","nachum","nachche"...]
function refreshUsersList(users) {
    //clear all current users
    $("#userslist").empty();
    
    // rebuild the list of users: scan all users and add them to the list of users
    $.each(users || [], function(index, username) {
        console.log("Adding user #" + index + ": " + username);
        //create a new <option> tag with a value in it and
        //appeand it to the #userslist (div with id=userslist) element
        $('<li>' + username + '</li>').appendTo($("#userslist"));
    });
}

//entries = the added chat strings represented as a single string
function appendToChatArea(entries) {
//    $("#chatarea").children(".success").removeClass("success");
    
    // add the relevant entries
    $.each(entries || [], appendChatEntry);
    
    // handle the scroller to auto scroll to the end of the chat area
    var scroller = $("#chatarea");
    var height = scroller[0].scrollHeight - $(scroller).height();
    $(scroller).stop().animate({ scrollTop: height }, "slow");
}

function appendChatEntry(index, entry){
    var entryElement = createChatEntry(entry);

    $("#chatarea").append(entryElement);
}

function createChatEntry (entry){
    entry.chatString = entry.chatString.replace (":)", "<img class='smiley-image' src='../../common/images/smiley.png'/>");
    var finalHtmlEntry = $(($("#username").text().substring($("#username").text().indexOf(" ")+1, $("#username").text().lastIndexOf(".")) === entry.username ?
        "<div class=\"balon2 p-2 m-0 w-100 d-flex align-items-start flex-column\">\n":
        "<div class=\"balon1 p-2 m-0 w-100 d-flex align-items-end flex-column\">\n") +
        "                                    <a class=\"text-break w-100\">"+entry.chatString+"</a>\n" +
        ($("#username").text().substring($("#username").text().indexOf(" ")+1, $("#username").text().lastIndexOf(".")) === entry.username ?
            "                                    <span class=\"\">"+entry.username+"</span>\n" :
            "                                    <span class=\"float-right\">"+entry.username+"</span>\n")
            +
        "                            </div>");
    return finalHtmlEntry;
}

function ajaxUsersList() {
    $.ajax({
        url: USER_LIST_URL,
        success: function(users) {
            refreshUsersList(users);
        }
    });
}

//call the server and get the chat version
//we also send it the current chat version so in case there was a change
//in the chat content, we will get the new string as well
function ajaxChatContent() {
    $.ajax({
        url: CHAT_LIST_URL,
        data: "chatversion=" + chatVersion,
        dataType: 'json',
        success: function(data) {
            /*
             data will arrive in the next form:
             {
                "entries": [
                    {
                        "chatString":"Hi",
                        "username":"bbb",
                        "time":1485548397514
                    },
                    {
                        "chatString":"Hello",
                        "username":"bbb",
                        "time":1485548397514
                    }
                ],
                "version":1
             }
             */
            console.log("Server chat version: " + data.version + ", Current chat version: " + chatVersion);
            if (data.version !== chatVersion) {
                chatVersion = data.version;
                appendToChatArea(data.entries);
            }
            triggerAjaxChatContent();
        },
        error: function(error) {
            triggerAjaxChatContent();
        }
    });
}

//add a method to the button in order to make that form use AJAX
//and not actually submit the form
$(function() { // onload...do
    //add a function to the submit event
    $("#btn-chat").click(function() {
        $.ajax({
            data: {"userstring": $('#btn-input').val()},
            url: SEND_CHAT_URL,
            timeout: 2000,
            error: function() {
                console.error("Failed to submit");
            },
            success: function(r) {
                //do not add the user string to the chat area
                //since it's going to be retrieved from the server
                //$("#result h1").text(r);
            }
        });

        $("#userstring").val("");
        // by default - we'll always return false so it doesn't redirect the user.
        return false;
    });
});

function triggerAjaxChatContent() {
    setTimeout(ajaxChatContent, refreshRate);
}

//activate the timer calls after the page is loaded
$(function() {

    //The users list is refreshed automatically every second
    setInterval(ajaxUsersList, refreshRate);
    
    //The chat content is refreshed only once (using a timeout) but
    //on each call it triggers another execution of itself later (1 second later)
    triggerAjaxChatContent();
});