var USER_DETAILS_URL = buildUrlWithContextPath("details");
var UPLOAD_URL = buildUrlWithContextPath("upload");
var All_USERS_URL = buildUrlWithContextPath("allUsersDetails");
var FORK_URL = buildUrlWithContextPath("fork");
var NOTIFICATIONS_URL = buildUrlWithContextPath("notifications");
var refreshRate = 2000;
var h = document.cookie;
var s;
var accountDetails;
var repoDetailsInterval;
var notificationsversion = 0;
var numOfNotifications = 0;
// {"userName":"gh","repositories":{"banana":{"commitMessage":"msg..","name":"repo name","commitDate":"5/5/15","branchesNum":"5","activeBranch":"branch"}, ..}
function createRepository(repoId, details){
    var repository = $("<div class=\"col-xl-3 col-sm-12 col-md-6\">\n" +
        "        <div class=\"mb-2 card border text-dark bg-light\">\n" +
        "           <div class=\"card-header bg-dark align-baseline\">" +
        "               <span class=\"mt-1 badge badge-primary badge-pill float-left text-left text-light\">" + repoId + "</span>\n" +
        "               <div class=\"text-center text-light\">"+details.name+"</div>\n" +
        "           </div>\n" +
        //"            <div class=\"card-header bg-dark text-center text-light\">"+details.name+"</div>\n" +
        "            <div class=\"card-body\">\n" +
        "                <h5 class=\"card-title\">Repository details</h5>\n" +
        "                \n" +
        "                <div class=\"col-xl-12 col-sm-12\">\n" +
        "                    Active branch:" +
        "                    <span class=\"text-wrap text-break font-weight-bold\">"+details.activeBranch+"</span>\n" +
        "                </div>\n" +
        "                <div class=\"col-xl-12 col-sm-12\">\n" +
        "                    Branches:" +
        "                    <span class=\"text-wrap text-break font-weight-bold\">"+details.branchesNum+"</span>\n" +
        "                </div>\n" +
        "                <div class=\"col-xl-12 col-sm-12\">\n" +
        "                    Commit date:" +
        "                    <span class=\"text-wrap text-break font-weight-bold\">"+details.commitDate+"</span>\n" +
        "                </div>\n" +
        "                <div class=\"col-xl-12 col-sm-12\">\n" +
        "                    Commit message:" +
        "                    <span class=\"text-wrap text-break font-weight-bold\">"+details.commitMessage+"</span>\n" +
        "                </div>\n" +
        "               <br>\n" +
        "                <a " +"id="+ repoId+ " href=\"#\" class=\"btn btn-primary rep-details\">Manage</a>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>")
    var repository1 = $("" +
        "<div class=\"col-xl-12 col-sm-12 mb-12\">" +
        "<div class=\"card text-dark bg-light\">\n" +
        "    <div id=\"card-header-edit\"  class=\"card-header bg-dark text-center text-light\"><h4>"+details.name+"</h4></div>\n" +
        "      <div class=\"card-body \">\n" +
        "        <h5 class=\"card-title\">Repository details</h5>\n" +
        "        <!--Starting list group here -->\n" +
        "            <div class=\"d-flex row\">\n" +
        "              <div class=\"col-xl-3 col-sm-12 mb-3 list-group-item d-flex justify-content-between align-items-center list-group-item-action list-group-item-primary\">Active branch\n" +
        "              <span class=\"text-wrap text-break badge badge-primary badge-pill\">" + details.activeBranch + "</span>\n" +
        "              </div>\n" +
        "              <div class=\"col-xl-3 col-sm-12 mb-3 list-group-item d-flex justify-content-between align-items-center list-group-item-action list-group-item-danger\">Branches\n" +
        "              <span class=\"text-wrap text-break badge badge-primary badge-pill\">"+details.branchesNum+"</span>\n" +
        "              </div>\n" +
        "              <div class=\"col-xl-3 col-sm-12 mb-3 list-group-item d-flex justify-content-between align-items-center list-group-item-action list-group-item-success\">\n" +
        "                Commit date\n" +
        "                <span class=\"text-wrap text-break badge badge-primary badge-pill commit-details\">"+details.commitDate+"</span>    \n" +
        "              </div>\n" +
        "              <div class=\"col-xl-3 col-sm-12 mb-3 list-group-item d-flex justify-content-between align-items-center list-group-item-action list-group-item-info\">Commit Message\n" +
        "              <span class=\"text-wrap text-break badge badge-primary badge-pill commit-details\">"+details.commitMessage+"</span>\n" +
        "              </div>\n" +
        "            </div>\n" +
        "          <!--Ends here -->  \n" +
        "      </div>\n" +
        "      <div id=\"card-footer-edit\" class=\"card-footer bg-secondary border-danger text-right\">\n" +
        "      <a " +"id="+ repoId+ " href=\"#\" class=\"btn btn-info btn-sm rep-details\">Manage</a>\n" +
        "      </div>\n" +
        "    </div>" +
        "</div>").addClass("square").text(repoId.height);

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
                if(r === ""){
                    successToast("Repository uploaded successfully!",false, 6000);
                    //$("#uploadMessage").removeClass("alert-danger").addClass("alert-success").empty().append("<h6> Repository uploaded successfully! </h6>").fadeIn(500).delay(5000).fadeOut();
                }
                else
                    errorToast(r, false, 6000);
                    //$("#uploadMessage").addClass("alert-danger").removeClass("alert-success").empty().append("<h6>" + r + "</h6>").fadeIn(500).delay(5000).fadeOut();

                //  $("#result").text(r);
            }
        });
        return false;
    });
};

function userNameClicked() { ////////////// todo remove
    stopShowingRepositories();
    saveState("#repositoriesbutton");
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
        "                <th class=\"th-lg\"><a># <i class=\"ml-1\"></i></a></th>\n" +
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
            var cloneId = 'clone' + userName + repositoryId;
            $("tbody", user).append("             <tr>\n" +
                "                <td>"+ repositoryId +"</td>\n" +
                "                <td>"+ repository.name +"</td>\n" +
                "                <td>" + repository.activeBranch + "</td>\n" +
                "                <td>" + repository.branchesNum + "</td>\n" +
                "                <td>" + repository.commitDate + "</td>\n" +
                "                <td>" + repository.commitMessage + "</td>\n" +
                "                <td>" +
                "                  <a><i id="+ cloneId + " class=\"fas fa-clone mx-1\" data-toggle=\"modal\" data-target=\"#forkRepoModal\" data-username=" + userName +" data-id=" + repositoryId + " data-reponame=" + "'" + repository.name + "'" + " data-placement=\"top\"\n" +
                "                      title=\"Clone repository\"></i></a>\n" +
                "                </td>\n" +
                "              </tr>\n" +
                "              <tr>\n");
        });

    $('#accordionEx78').append(user);
}
function forkRepository(event) {
    var cloneName = $('#forkRepoModal #repository-name').val();
    $.ajax({
        data: { userName : event.data.userName, repositoryToFork : event.data.repositoryId,repositoryName : cloneName},
        url: FORK_URL,
        timeout: 2000,
        error: function() {

        },
        success: function(msg) {
            if(msg.trim() === "") {
                $("#forkMessage").removeClass("alert-danger").addClass("alert-success").empty().append("<h6> Repository forked successfully! </h6>").fadeIn(500).delay(2000).fadeOut();
                $('#forkRepoModal #forkButton').prop('disabled', true);
                $("#forkRepoModal").modal('toggle');
                //setTimeout(function(){$("#forkRepoModal").modal('toggle')},2600);
                successToast('Repository forked successfully!',false,6000);
            }
            else
                //$("#forkMessage").addClass("alert-danger").removeClass("alert-success").empty().append("<h6>" + msg + "</h6>").fadeIn(500).delay(2000).fadeOut();
                errorToast(msg,true);
            //  $("#result").text(r);
        }
    });
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
                "<div class=\"modal fade\" id=\"forkRepoModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\n" +
                "  <div class=\"modal-dialog\" role=\"document\">\n" +
                "    <div class=\"modal-content\">\n" +
                "      <div class=\"modal-header\">\n" +
                "        <h5 class=\"modal-title\" id=\"exampleModalLabel\">New message</h5>\n" +
                "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">\n" +
                "          <span aria-hidden=\"true\">&times;</span>\n" +
                "        </button>\n" +
                "      </div>\n" +
                "      <div class=\"modal-body\">\n" +
                "        <form>\n" +
                "          <div class=\"form-group\">\n" +
                "            <label for=\"recipient-name\" class=\"col-form-label\">Repository name:</label>\n" +
                "            <input type=\"text\" class=\"form-control\" id=\"repository-name\">\n" +
                "          </div>\n" +
                "        </form>\n" +
                "      <div id='forkMessage' class=\"alert alert-danger\" style=\"display:none\"></div>" +
                "      </div>\n" +
                "      <div class=\"modal-footer\">\n" +
                "        <button type=\"button\" class=\"btn btn-secondary\" data-dismiss=\"modal\">Close</button>\n" +
                "        <button id=\"forkButton\" type=\"button\" class=\"btn btn-primary\" value=\"Ok\">Fork</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</div>" +
                "</div>");
            $('#forkRepoModal',usersAccor).on('show.bs.modal', function (event) {
                var button = $(event.relatedTarget) // Button that triggered the modal
                var userName = button.data('username'); // Extract info from data-* attributes
                var id = button.data('id');
                var repositoryName = button.data('reponame');
                // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
                // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
                var modal = $(this);
                $('#forkRepoModal #forkButton').prop('disabled', false);
                modal.find('.modal-title').text('Forking ' + repositoryName + ' from ' + userName);
                modal.find('#forkButton').off('click').on('click',{userName: userName, repositoryId: id},forkRepository);
            });
            $('#users-container').append(usersAccor);

            $.each(users || [], createUser);
        }
    });
    saveState("#users");
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
        '<input type="text" class="form-control" placeholder="Choose XML repository file..." />'+
        '<span class="input-group-btn">'+
        '<button class="btn btn-secondary btn-choose" type="button">Browse</button>'+
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
$(function (){
    setInterval(function () {
        $.ajax({
            url: NOTIFICATIONS_URL,
            data: {notificationsversion : notificationsversion, getCount : true},
            dataType: 'json',
            success: function(data) {
                console.log("new messages: " + data);
                $('#noti_Counter').text(data);
                if(data > 0 && $('#noti_Counter').css('display') === 'none'){
                    $('#noti_Counter')
                        .css({ opacity: 0, display: 'block'})
                        .text(data)  // ADD DYNAMIC VALUE (YOU CAN EXTRACT DATA FROM DATABASE OR XML).
                        .css({ top: '-10px' })
                        .animate({ top: '-2px', opacity: 1 }, 500);
                        $('#noti_Button').css('background-color', '#FFF');
                }else{
                   // if(!($('#noti_Counter').css('display') === 'none') && $('#notifications').css('display') === 'none'){
                   //     $('#noti_Counter').hide();
                   //     $('#noti_Button').css('background-color', '#2E467C');
                   // }
                }
            },
            error: function(error) {
                window.location.href = error.getResponseHeader("Location");
                localStorage["pageState"] = "";
            }
        });
    },1500);
});


$(document).ready(function () {
    // ANIMATEDLY DISPLAY THE NOTIFICATION COUNTER.
    $(".toast").toast();
    $('#noti_Counter')
        .css({ opacity: 0})
        .text(numOfNotifications)  // ADD DYNAMIC VALUE (YOU CAN EXTRACT DATA FROM DATABASE OR XML).
        .css({ top: '-10px' })
        .animate({ top: '-2px', opacity: 1 }, 500);
    $('#noti_Counter').hide();
    $('#noti_Button').css('background-color', '#2E467C');
    $('#noti_Button').click(function () {
        ajaxNotificationsContent();
        // TOGGLE (SHOW OR HIDE) NOTIFICATION WINDOW.
        $('#notifications').fadeToggle('fast', 'linear', function () {
            if ($('#notifications').is(':hidden')) {
                $('#noti_Button').css('background-color', '#2E467C');
            }
            // CHANGE BACKGROUND COLOR OF THE BUTTON.
            else $('#noti_Button').css('background-color', '#FFF');
        });

        $('#noti_Counter').fadeOut('slow');     // HIDE THE COUNTER.
        return false;
    });

    // HIDE NOTIFICATIONS WHEN CLICKED ANYWHERE ON THE PAGE.
    $(document).click(function () {
        $('#notifications').hide();

        // CHECK IF NOTIFICATION COUNTER IS HIDDEN.
        if ($('#noti_Counter').is(':hidden')) {
            // CHANGE BACKGROUND COLOR OF THE BUTTON.
            $('#noti_Button').css('background-color', '#2E467C');
        }
    });

    $('#notifications').click(function () {
        return false;       // DO NOTHING WHEN CONTAINER IS CLICKED.
    });
});

$(function () {
    $('#minim_chat_window').on('click', function (e) {
        var $this = $(this);
        if (!$this.hasClass('panel-collapsed')) {
            $this.parents('.panel').find('.panel-body').slideUp();
            $this.addClass('panel-collapsed');
            $this.removeClass('fa-minus').addClass('fa-plus');
        } else {
            $this.parents('.panel').find('.panel-body').slideDown();
            $this.removeClass('panel-collapsed');
            $this.removeClass('fa-plus').addClass('fa-minus');
        }
    });
    $('.icon_close').on('click', function (e) {
        //$(this).parent().parent().parent().parent().remove();
        $( "#chat_window_1" ).remove();
    });

    $('#btn-input').keypress(function(event){

        var keycode = (event.keyCode ? event.keyCode : event.which);
        if(keycode == '13'){
            $('#btn-chat').click();
        }

    });
    $('#minim_chat_window').parents('.panel').find('.panel-body').slideUp(0);
    $('#minim_chat_window').removeClass('fa-minus').addClass('fa-plus');
});


function ajaxNotificationsContent() {
    $.ajax({
        url: NOTIFICATIONS_URL,
        data: {notificationsversion : notificationsversion, getCount : false},
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
            console.log("Server chat version: " + data.version + ", Current chat version: " + notificationsversion);
            if (data.version !== notificationsversion) {
                notificationsversion = data.version;
                appendToNotificationsArea(data.entries);
            }
            //triggerAjaxNotificationsContent();
        },
        error: function(error) {
            //triggerAjaxNotificationsContent();
        }
    });
}

//entries = {"entries":[{"message":"mymsg","username":"myusername","time":1569613672373},{"message":"mymsg1","username":"myusername1","time":1569613672373},{"message":"mymsg2","username":"myusername2","time":1569613672373}],"version":3}
function appendToNotificationsArea(entries) {
//    $("#chatarea").children(".success").removeClass("success");
    // add the relevant entries
    $.each(entries || [], appendNotificationEntry);



    // handle the scroller to auto scroll to the end of the chat area
    //var scroller = $("#chatarea");
    //var height = scroller[0].scrollHeight - $(scroller).height();
    //$(scroller).stop().animate({ scrollTop: height }, "slow");
}

function appendNotificationEntry(index, entry){
    var entryElement = createNotificationEntry(entry);
    $("#notificationsArea").prepend(entryElement);
}

function createNotificationEntry (entry){
   // entry.chatString = entry.chatString.replace (":)", "<img class='smiley-image' src='../../common/images/smiley.png'/>");
    return $("<div class=\"w-100 p-1 toast fade show toast-notification\">\n" +
        "                                    <div class=\"toast-header\">\n" +
        "                                        <strong class=\"mr-auto\"><i class=\"fa fa-globe\"></i> "+ entry.username+" </strong>\n" +
        "                                        <small class=\"text-muted\"> "+ new Date(entry.time).toLocaleString()+" </small>\n" +
        "                                    </div>\n" +
        "                                    <div class=\"toast-body\"> "+entry.message+" </div>\n" +
        "                                </div>");
}


