var USER_DETAILS_URL = buildUrlWithContextPath("details");
$(function () {
    $.ajax({
        data: $(this).serialize(),
        url: USER_DETAILS_URL,
        timeout: 2000,
        error: function() {

        },
        success: function(msg) {
            //{"userName":"Guy","repositories":{}}
            accountDetails = msg;
            $("#username").empty().append('Hello, ').append(msg.userName).append(".");

        }
    });
});
$(function () {
    if(localStorage["pageState"] === "" || localStorage["pageState"] === undefined){
        saveState("#repositoriesbutton")
    }
    //$("#username").click(userNameClicked);
    $("#repositoriesbutton").click(showRepositoriesPage);
    $("#users").click(showUsersPage);
    $("#logout").click(logout);

    bindNavClick();
    //bindNotificationsClick();
    resumeState();
});

function bindNavClick(){
    $( ".navbar-nav .nav-item" ).bind( "click", function(event) {
        event.preventDefault();
        var clickedItem = $( this );
        if(clickedItem.children().attr("id") === "username"){
            return;
        }
        $( ".navbar-nav .nav-item" ).each( function() {
            $( this ).removeClass( "active" );
        });
        clickedItem.addClass( "active" );
    });
}

function supportsLocalStorage() {
    return ('localStorage' in window) && window['localStorage'] !== null;
}

function saveState(func) {
    if (!supportsLocalStorage()) { return false; }
    localStorage["pageState"] = func;
}
function resumeState() {
    if (!supportsLocalStorage()) { return false; }
    var resumeId = localStorage["pageState"];
    if(resumeId === "empty"){
        emptyContainers();
        return;
    }
    $(resumeId).trigger("click");
}
function logout() {
    stopShowingRepositories();
    var LOUGOUT_URL = buildUrlWithContextPath("/pages/signup/logout");
    $.ajax( {
        url:LOUGOUT_URL,
        timeout:2000,
        error: function (a) {
            window.location.href = a.getResponseHeader("Location");
            localStorage["pageState"] = "";
            localStorage["seenNotifications"] = 0;
        },
        success: function () {}
    });
    localStorage["pageState"] = "";
}

$(function() {
    var str = window.location.href;
    if (str.includes("#repositoriesbutton")) {
        showRepositoriesPage();
    }
});