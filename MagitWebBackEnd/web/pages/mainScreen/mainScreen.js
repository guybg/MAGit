var USER_DETAILS_URL = buildUrlWithContextPath("details");
var UPLOAD_URL = buildUrlWithContextPath("upload");
var All_USERS_URL = buildUrlWithContextPath("allUsersDetails");
var refreshRate = 2000;
var h = document.cookie;
var s;
var accountDetails;
var repoDetailsInterval;

// {"userName":"gh","repositories":{"banana":{"commitMessage":"msg..","name":"repo name","commitDate":"5/5/15","branchesNum":"5","activeBranch":"branch"}, ..}
function createRepository(repoId, details){
    var repository = $("<div class=\"col-xl-3 col-sm-6 mb-3\">" +
        "<div class=\"card text-white bg-dark mb-3 o-hidden h-100\">" +
        "<div class=\"card-body\">" +
        "<div class=\"card-body-icon\">" +
        "<i class=\"fas fa-fw fa-tv\"></i>" +
        "</div>" +
        "<div class=\"mr-5\">" +
        "Repository: "  + details.name +
        "</div>" +
        "<div class=\"mr-5\">" +
        "Active branch: " + details.activeBranch +
        "</div>" +
        "<div class=\"mr-5\">" +
        "Branches: " + details.branchesNum +
        "</div>" +
        "<div class=\"mr-5 commit-details\">" +
        "Commit date: " + details.commitDate +
        "</div>" +
        "<div class=\"mr-5 commit-details\">" +
        "Commit message: " + details.commitMessage +
        "</div>" +
        "</div>" +
        "<a class=\"card-footer text-white clearfix small z-1\" href=\"#\">" +
        "<span" +
        " id=" +
        repoId +
        ' class=\"float-left rep-details\">View Details</span>' +
        "<span class=\"float-right\">" +
        "<i class=\"fas fa-angle-right rep-details\"></i>" +
        "</span>" +
        "</a>" +
        "</div>" +
        "</div>")
        .addClass("square")
        .text(repoId.height);

    if(details.commitDate === "No commit"){
        $(".commit-details",repository).hide();
    }

    $('#repository-container').append(repository);

    //$(".rep-details").attr('id', repoId);
    $(".rep-details").click(toRepositoryDetailsPage);
}


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


function uploadAjaxSubmit() {
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
                if(r === "")
                    $("#uploadMessage").removeClass("alert-danger").addClass("alert-success").empty().append("<h6> Repository uploaded successfully! </h6>").fadeIn(500).delay(5000).fadeOut();
                else
                    $("#uploadMessage").addClass("alert-danger").removeClass("alert-success").empty().append("<h6>" + r + "</h6>").fadeIn(500).delay(5000).fadeOut();
              //  $("#result").text(r);
            }
        });
        return false;
    });
};

function userNameClicked() {
    stopShowingRepositories();
    saveState("#username");
    emptyContainers();
    $("#main-container").append(
        "      <div class=\"my-3 p-3 bg-light rounded box-shadow\">\n" +
        "        <h6 class=\"border-bottom border-gray pb-2 mb-0\">Recent updates</h6>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=007bff&fg=007bff&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <p class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <strong class=\"d-block text-gray-dark\">@username</strong>\n" +
        "            Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.\n" +
        "          </p>\n" +
        "        </div>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=e83e8c&fg=e83e8c&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <p class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <strong class=\"d-block text-gray-dark\">@username</strong>\n" +
        "            Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.\n" +
        "          </p>\n" +
        "        </div>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=6f42c1&fg=6f42c1&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <p class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <strong class=\"d-block text-gray-dark\">@username</strong>\n" +
        "            Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.\n" +
        "          </p>\n" +
        "        </div>\n" +
        "        <small class=\"d-block text-right mt-3\">\n" +
        "          <a href=\"#\">All updates</a>\n" +
        "        </small>\n" +
        "      </div>\n" +
        "\n" +
        "      <div class=\"my-3 p-3 bg-light rounded box-shadow\">\n" +
        "        <h6 class=\"border-bottom border-gray pb-2 mb-0\">Chat</h6>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=007bff&fg=007bff&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <div class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <div class=\"d-flex justify-content-between align-items-center w-100\">\n" +
        "              <strong class=\"text-gray-dark\">Full Name</strong>\n" +
        "              <a href=\"#\">Follow</a>\n" +
        "            </div>\n" +
        "            <span class=\"d-block\">@username</span>\n" +
        "          </div>\n" +
        "        </div>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=007bff&fg=007bff&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <div class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <div class=\"d-flex justify-content-between align-items-center w-100\">\n" +
        "              <strong class=\"text-gray-dark\">Full Name</strong>\n" +
        "              <a href=\"#\">Follow</a>\n" +
        "            </div>\n" +
        "            <span class=\"d-block\">@username</span>\n" +
        "          </div>\n" +
        "        </div>\n" +
        "        <div class=\"media text-muted pt-3\">\n" +
        "          <img data-src=\"holder.js/32x32?theme=thumb&bg=007bff&fg=007bff&size=1\" alt=\"\" class=\"mr-2 rounded\">\n" +
        "          <div class=\"media-body pb-3 mb-0 small lh-125 border-bottom border-gray\">\n" +
        "            <div class=\"d-flex justify-content-between align-items-center w-100\">\n" +
        "              <strong class=\"text-gray-dark\">Full Name</strong>\n" +
        "              <a href=\"#\">Follow</a>\n" +
        "            </div>\n" +
        "            <span class=\"d-block\">@username</span>\n" +
        "          </div>\n" +
        "        </div>\n" +
        "        <small class=\"d-block text-right mt-3\">\n" +
        "          <a href=\"#\">All suggestions</a>\n" +
        "        </small>\n" +
        "      </div>\n");
}
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
            $("#repository-container").empty();
            var repositories = msg.repositories;
            $.each(repositories || [], createRepository);
        }
    });
}
function showUsers(users) {
    $("#users-container").append(users);
}
function createUser() {
    var user = $(
        "<div class=\"col-xl-3 col-sm-6 mb-3\">" +
        "<div class=\"accordion\" id=\"accordionExample\">\n" +
        "  <div class=\"card\">\n" +
        "    <div class=\"card-header\" id=\"headingOne\">\n" +
        "      <h2 class=\"mb-0\">\n" +
        "        <button class=\"btn btn-link\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapseOne\" aria-expanded=\"true\" aria-controls=\"collapseOne\">\n" +
        "          Collapsible Group Item #1\n" +
        "        </button>\n" +
        "      </h2>\n" +
        "    </div>\n" +
        "\n" +
        "    <div id=\"username1\" class=\"collapse show\" aria-labelledby=\"headingOne\" data-parent=\"#accordionExample\">\n" +
        "      <div class=\"card-body\">\n" +
        "        Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "  <div class=\"card\">\n" +
        "    <div class=\"card-header\" id=\"headingTwo\">\n" +
        "      <h2 class=\"mb-0\">\n" +
        "        <button class=\"btn btn-link collapsed\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapseTwo\" aria-expanded=\"false\" aria-controls=\"collapseTwo\">\n" +
        "          Collapsible Group Item #2\n" +
        "        </button>\n" +
        "      </h2>\n" +
        "    </div>\n" +
        "    <div id=\"collapseTwo\" class=\"collapse\" aria-labelledby=\"headingTwo\" data-parent=\"#accordionExample\">\n" +
        "      <div class=\"card-body\">\n" +
        "        Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "  <div class=\"card\">\n" +
        "    <div class=\"card-header\" id=\"headingThree\">\n" +
        "      <h2 class=\"mb-0\">\n" +
        "        <button class=\"btn btn-link collapsed\" type=\"button\" data-toggle=\"collapse\" data-target=\"#collapseThree\" aria-expanded=\"false\" aria-controls=\"collapseThree\">\n" +
        "          Collapsible Group Item #3\n" +
        "        </button>\n" +
        "      </h2>\n" +
        "    </div>\n" +
        "    <div id=\"collapseThree\" class=\"collapse\" aria-labelledby=\"headingThree\" data-parent=\"#accordionExample\">\n" +
        "      <div class=\"card-body\">\n" +
        "        Anim pariatur cliche reprehenderit, enim eiusmod high life accusamus terry richardson ad squid. 3 wolf moon officia aute, non cupidatat skateboard dolor brunch. Food truck quinoa nesciunt laborum eiusmod. Brunch 3 wolf moon tempor, sunt aliqua put a bird on it squid single-origin coffee nulla assumenda shoreditch et. Nihil anim keffiyeh helvetica, craft beer labore wes anderson cred nesciunt sapiente ea proident. Ad vegan excepteur butcher vice lomo. Leggings occaecat craft beer farm-to-table, raw denim aesthetic synth nesciunt you probably haven't heard of them accusamus labore sustainable VHS.\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "</div>" +
        "</div>" +
    "           </div>");
    $('#users-container').append(user);
    $('.collapse').collapse("toggle");
}
function showUsersPage() {
    $.ajax({
        data: $(this).serialize(),
        url: All_USERS_URL,
        timeout: 2000,
        error: function() {

        },
        success: function(msg) {
            stopShowingRepositories();
            emptyContainers();
            var users = msg;
            $.each(users || [], createUser);
        }
    });
}

function showRepositoriesPage() {
    emptyContainers();
    showRepositories();

    $("#repository-upload").append(
        '<div class="col-xl-3 col-sm-6 mb-3">'+
        '<form id="uploadForm" method="POST" action="/upload" enctype="multipart/form-data">'+
        <!-- COMPONENT START -->
        '<div class="form-group">'+
        '<div class="input-group input-file" name="Fichier1">' +
        '<input type="text" class="form-control" placeholder="Choose a file..." />'+
        '<span class="input-group-btn">'+
        '<button class="btn btn-secondary btn-choose" type="button">Choose</button>'+
        '</span>'+
        '</div>'+
        '</div>'+
        <!-- COMPONENT END -->
        '<div class="form-group">'+
        '<button type="submit" class="btn btn-primary pull-right">Submit</button>'+
        '&nbsp;&nbsp;&nbsp;' +
        '<button type="reset" class="btn btn-danger">Reset</button>'+
        '</div>'+
        '<div id="uploadMessage" class="alert alert-danger"></div>' +
        '</form>'+
        '</div>');
    bs_input_file();
    uploadAjaxSubmit();
    $("#uploadMessage").hide();
    $("#inputGroupFile01").change(updateInputLabel);
    repoDetailsInterval = setInterval(showRepositories, refreshRate);
    saveState("#repositoriesbutton");
}

function toRepositoryDetailsPage() {
    saveState("empty");
    stopShowingRepositories();
    var REPO_DETAILS_URL = buildUrlWithContextPath("repodetails");
    $.ajax( {
        type: 'GET',
        data: {
            "username": accountDetails["userName"],
            "id" : $(this).attr('id')
        },
        url: REPO_DETAILS_URL,
        timeout: 2000,
        error : function () {},
        success: function (a) {
            window.location.href = "../repositoryDetails/repositoryDetails.html";
            getRepositoryDetails(a);
        }
    });
}

function stopShowingRepositories() {
    clearInterval(repoDetailsInterval);
}




