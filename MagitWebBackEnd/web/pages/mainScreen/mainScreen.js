var USER_DETAILS_URL = buildUrlWithContextPath("details");
var h = document.cookie;
var accountDetails;
// dataJson = {color: "" ; height: ""}
function createRepository(location, name){
    var repository = $("<div class=\"col-xl-3 col-sm-6 mb-3\">" +
        "<div class=\"card text-white bg-primary o-hidden h-100\">" +
        "<div class=\"card-body\">" +
        "<div class=\"card-body-icon\">" +
        "<i class=\"fas fa-fw fa-comments\"></i>" +
        "</div>" +
        "<div class=\"mr-5\">" +
        name +
        "</div>" +
        "</div>" +
        "<a class=\"card-footer text-white clearfix small z-1\" href=\"#\">" +
        "<span class=\"float-left\">View Details</span>" +
        "<span class=\"float-right\">" +
        "<i class=\"fas fa-angle-right\"></i>" +
        "</span>" +
        "</a>" +
        "</div>" +
        "</div>")
        .attr('id', location)
        .attr('name', name)
        .addClass("square")
        .text(name.height);

    $('#repository-container').append(repository);
}

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
    $("#repositoriesbutton").click(showRepositories);
    $("#logout").click(logout);
})
function showRepositories() {
    $.ajax({
        data: $(this).serialize(),
        url: USER_DETAILS_URL,
        timeout: 2000,
        error: function() {

        },
        success: function(msg) {
            //{"userName":"Guy","repositories":{}}
            $("#repository-container").empty();
            var repositories = msg.repositories;
            $.each(repositories || [], createRepository);
        }
    });
}

function logout() {
    var LOUGOUT_URL = buildUrlWithContextPath("pages/signup/logout");
    $.ajax( {
        url:LOUGOUT_URL,
        timeout:2000,
        error: function (a) {
            window.location.href = a.getResponseHeader("Location");
        },
        success: function () {}
    });
}

