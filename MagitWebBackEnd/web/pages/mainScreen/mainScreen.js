var USER_DETAILS_URL = buildUrlWithContextPath("details");
var UPLOAD_URL = buildUrlWithContextPath("upload");
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
function bs_input_file() {
    $(".input-file").before(
        function() {
            if ( ! $(this).prev().hasClass('input-ghost') ) {
                var element = $("<input type='file' class='input-ghost' style='visibility:hidden; height:0'>");
                element.attr("name",$(this).attr("name"));
                element.change(function(){
                    element.next(element).find('input').val((element.val()).split('\\').pop());
                });
                $(this).find("button.btn-choose").click(function(){
                    element.click();
                });
                $(this).find("button.btn-reset").click(function(){
                    element.val(null);
                    $(this).parents(".input-file").find('input').val('');
                });
                $(this).find('input').css("cursor","pointer");
                $(this).find('input').mousedown(function() {
                    $(this).parents('.input-file').prev().click();
                    return false;
                });
                return element;
            }
        }
    );
}
$(function () {
    $("#repositoriesbutton").click(showRepositories);
    bs_input_file();

})

$(function() { // onload...do
    $("#uploadForm").submit(function () {

        var file = this[0].files[0];

        var formData = new FormData();
        formData.append("fake-key-1", file);

        $.ajax({
            method: 'POST',
            data: formData,
            url: UPLOAD_URL,
            processData: false, // Don't process the files
            contentType: false, // Set content type to false as jQuery will tell the server its a query string request
            timeout: 4000,
            error: function (e) {
                console.error("Failed to submit");
               // $("#result").text("Failed to get result from server " + e);
            },
            success: function (r) {
              //  $("#result").text(r);
            }
        });
        return false;
    });
});

function updateInputLabel() {
    //get the file name
    var fileName = $(this).val();
    //replace the "Choose a file" label
    $(this).next('.custom-file-label').html(fileName);
}
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
            $("#repository-upload").empty();
            $("#repository-upload").append(
                '<div class="col-xl-6 col-sm-8 mb-3">' +
                    '<form class="md-form" id="uploadForm" action="/upload" enctype="multipart/form-data" method="POST">'+
                        '<div class="input-group">' +
                            '<div class="input-group-prepend">' +
                                '<span class="input-group-text" id="inputGroupFileAddon01">Upload repository</span>' +
                            '</div>' +
                            '<div class="custom-file">' +
                                '<input type="file" class="custom-file-input" id="inputGroupFile01"' +
                                'aria-describedby="inputGroupFileAddon01">' +
                            '<form>' +
                                '<label class="custom-file-label" for="inputGroupFile01">Choose file</label>' +
                            '</form>' +
                            '</div>' +
                            '</div>' +
                            '<div class="file-path-wrapper">' +
                            '<input type="Submit" value="Upload File" class="btn btn-primary">' +
                '</div>' +

                '   </form>' +
                '</div>');
            $("#inputGroupFile01").change(updateInputLabel);
            var repositories = msg.repositories;
            $.each(repositories || [], createRepository);
        }
    });




}








