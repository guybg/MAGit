var refreshRate = 2000;

function appendMessage(msg){
   // $('<span class=\\"bg-danger\\"><%=' + msg + "></span>").appendTo($("#bodydiv"));
    //$("#bodydiv").append("<span class=\"bg-danger\"><%=").append(msg).append(msg).append("></span>");
}

$(function() {
    $("#errorMessage").hide();
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

