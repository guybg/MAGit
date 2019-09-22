var refreshRate = 2000;
var LOGIN_URL = buildUrlWithContextPath("pages/signup/login");

function alreadyAUser(msg){
    $.ajax({
        type: "GET",
        data: $(this).serialize(),
        url: LOGIN_URL,
        timeout: 2000,
        error: function (a) {
            console.error("Failed to submit");
            window.location.href = a.getResponseHeader("Location");
        },
        success: function (output, status, xhr) {
            console.log("im at success" + output);
        }
    });
}

$(function() {
    $("#errorMessage").hide();
    alreadyAUser();
    $("#loginform").submit(function () {
        $.ajax({
            type: "GET",
            data: $(this).serialize(),
            url: this.action,
            timeout: 2000,
            error: function (a) {
                console.error("Failed to submit");
                window.location.href = a.getResponseHeader("Location");

            },
            success: function (output, status, xhr) {
                console.log("im at success" + output);
                $("#errorMessage").empty();
                $('<h6>' + output + '</h6>').appendTo($("#errorMessage"));
                $("#errorMessage").show();
            }
        });
        return false;
    });
});

