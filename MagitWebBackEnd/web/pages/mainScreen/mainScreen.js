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
    $("#accordionEx78").append(users);
}
//{"aaa":{"userName":"aaa","repositories":{"11":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"12":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"13":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"14":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"15":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"16":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"17":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"18":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"19":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"0":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"1":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"2":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"3":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"4":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"5":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"6":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"7":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"8":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"9":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"10":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"}},"userPath":"c:\\magit-ex3\\aaa","online":true},"Guy":{"userName":"Guy","repositories":{"11":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"12":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"13":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"0":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"1":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"2":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"3":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"4":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"5":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"6":{"branchesNum":"3","commitMessage":"small change","activeBranch":"deep","name":"rep 2221","commitDate":"Sun Jun 30 21:35:26 IDT 2019"},"7":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"8":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"9":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"},"10":{"branchesNum":"1","commitMessage":"No commit","activeBranch":"master","name":"empty","commitDate":"No commit"}},"userPath":"c:\\magit-ex3\\Guy","online":false}}
function createUser(userName, userAccount) {
    var headingId = 'heading' + userName;
    var collapseId = 'collapse' + userName;
    var status = '<span class="badge badge-danger">Offline</span>';
    if(userAccount.online){
        status = '<span class="badge badge-success">Online</span>';
    }
    var user = $(
        "\n" +
        "  \n" +
        " <!-- Accordion card -->\n" +
        "  <div class=\"card border-bottom border-gray pb-2 mb-0\">\n" +
        "\n" +
        "    <!-- Card header -->\n" +
        "    <div class=\"card-header\" role=\"tab\" id=" + headingId +">\n" +
        "\n" +
        "      <!--Options-->\n" +
        "      <div class=\"float-left\">\n" +
        "        <div class= data-toggle=\"dropdown\"\n" +
        "          aria-haspopup=\"true\" aria-expanded=\"false\"><i class=\"fas fa-user-circle\" style='vertical-align: bottom;'></i>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "\n" +
        "      <!-- Heading -->\n" +
        "      <a data-toggle=\"collapse\" data-parent=\"#accordionEx78\" href="+ "#" +collapseId +" aria-expanded=\"false\"\n" +
        "        aria-controls=" + collapseId + ">\n" +
        "        <h5 class=\"mt-1 mb-0\">\n" +
        "          <span>"+userName + "</span>\n" +
        "          <i class=\"fas fa-angle-down rotate-icon\"></i>\n" +
        "        </h5>\n" +
        "          <span>"+ status + "</span>" +
        "      </a>\n" +
        "\n" +
        "    </div>\n" +
        "\n" +
        "    <!-- Card body -->\n" +
        "    <div id=" + collapseId + " class=\"collapse\" role=\"tabpanel\" aria-labelledby=" + headingId + "\n" +
        "      data-parent=\"#accordionEx78\">\n" +
        "      <div class=\"card-body\">\n" +
        "\n" +
        "        <!-- Table responsive wrapper -->\n" +
        "        <div class=\"table-responsive mx-3\">\n" +
        "          <!--Table-->\n" +
        "          <table class=\"table table-hover mb-0\">\n" +
        "<p class=\"font-weight-bold\">Repositories</p>" +
        "            <!--Table head-->\n" +
        "            <thead>\n" +
        "              <tr>\n" +
        "                <th class=\"th-lg\"><a>Name <i class=\"ml-1\"></i></a></th>\n" +
        "                <th class=\"th-lg\"><a>Active branch<i class=\"ml-1\"></i></a></th>\n" +
        "                <th class=\"th-lg\"><a>Branches<i class=\"ml-1\"></i></a></th>\n" +
        "                <th class=\"th-lg\"><a>Commit date<i class=\"ml-1\"></i></a></th>\n" +
        "                <th class=\"th-lg\"><a>Commit message<i class=\"ml-1\"></i></a></th>\n" +
        "                <th></th>\n" +
        "              </tr>\n" +
        "            </thead>\n" +
        "            <!--Table head-->\n" +
        "\n" +
        "            <!--Table body-->\n" +
        "            <tbody>\n" +
        "            </tbody>\n" +
        "            <!--Table body-->\n" +
        "          </table>\n" +
        "          <!--Table-->\n" +
        "        </div>\n" +
        "        <!-- Table responsive wrapper -->\n" +
        "\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  </div>\n" +
        "  <!-- Accordion card -->\n" +
        "\n" +
        "</div>\n");
        $.each(userAccount.repositories || [], function (repositoryId, repository) {
            $("tbody", user).append("             <tr>\n" +
                "                <td>"+ repository.name +"</td>\n" +
                "                <td>" + repository.activeBranch + "</td>\n" +
                "                <td>" + repository.branchesNum + "</td>\n" +
                "                <td>" + repository.commitDate + "</td>\n" +
                "                <td>" + repository.commitMessage + "</td>\n" +
                "                <td>" +
                "                  <a><i class=\"fas fa-info mx-1\" data-toggle=\"tooltip\" data-placement=\"top\"\n" +
                "                      title=\"Tooltip on top\"></i></a>\n" +
                "                  <a><i class=\"fas fa-pen-square mx-1\"></i></a>\n" +
                "                  <a><i class=\"fas fa-times mx-1\"></i></a>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "              <tr>\n");
        });


    $('#accordionEx78').append(user);
}
function showUsersPage() {
    stopShowingRepositories();
    $.ajax({
        data: $(this).serialize(),
        url: All_USERS_URL,
        timeout: 2000,
        error: function() {

        },
        success: function(msg) {
            emptyContainers();
            var users = msg;
            var usersAccor = $("<div class=\"col-xl-12 col-sm-12 mb-12\">" +
                "<!--Accordion wrapper-->\n" +
                "<div class=\"accordion md-accordion accordion-blocks\" id=\"accordionEx78\" role=\"tablist\"\n" +
                "  aria-multiselectable=\"true\">\n" +
                "<!--/.Accordion wrapper-->" +
                "</div>" +
                "</div>");
            $('#users-container').append(usersAccor);
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
    stopShowingRepositories();
    repoDetailsInterval = setInterval(showRepositories, refreshRate);
    saveState("#repositoriesbutton");
}

var repoId;

function toRepositoryDetailsPage() {
    saveState("empty");
    stopShowingRepositories();
    window.location.href = "../repositoryDetails/repositoryDetails.html?id=" + $(this).attr('id');
}


function stopShowingRepositories() {
    if(repoDetailsInterval !== undefined)
    clearInterval(repoDetailsInterval);
}




