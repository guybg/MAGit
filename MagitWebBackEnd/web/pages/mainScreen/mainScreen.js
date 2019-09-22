var USER_DETAILS_URL = buildUrlWithContextPath("details");
var h = document.cookie;

function getUserName() {


    $(function () {
        $.ajax({
            data: $(this).serialize(),
            url: USER_DETAILS_URL,
            timeout: 2000,
            error: function() {

            },
            success: function(msg) {
                //{"userName":"Guy","repositories":{}}
                $("#username").empty().append('Hello, ').append(msg.userName).append(".");
            }
        });
    });
}