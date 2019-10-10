$(function() {
    $( ".navbar-nav .nav-item").click(function() {
        window.location.href = "../mainScreen/mainScreen.html";
    });
    $("#create-branch-btn").click(createBranch);
    $('#create-branch-form').submit(function (e) {
        e.preventDefault();
        var id = window.location.href.split('=')[1];
        $.ajax({
            type: $(this).attr('method'),
            url: $(this).attr('action'),
            data: {branchName: $("#branch-name",this).val(), id: id},
            timeout: 2000,
            error: function (a) {
                $('.modal-body-error').text(a.responseText);
                $('#error-modal').modal('show');
            },
            success: function(a) {
                createBranchView($.parseJSON(a));
                $('#create-branch-modal').modal('hide');
            }
        })
    });
    $("#commit-message-form").submit(function (e) {
        e.preventDefault();
        $.ajax({
            type: $(this).attr('method'),
            url: $(this).attr('action'),
            data: {
                'id' : window.location.href.split('=')[1],
                'inputFromUser' : $("#commit-message").val()
            },
            error: function (err) {
                $('#commit-message-modal').modal('hide');
                errorToast(err.responseText, false, 3000);
                getRepositoryInfo();
            },
            success: function(msg) {
                $('#commit-message-modal').modal('hide');
                successToast(msg,false,3000);
                getRepositoryInfo();
            }
        })
    });
    setInterval(getRepositoryInfo,2000);
    getRepositoryInfo();

    $("#manage-prs").click(function () {
        showPullRequests();
    });
    $("#create-commit-history").click(getCommitsInfo);


    $("#create-pr").click(createPr);
    $('#create-pr-form').submit(function (e) {
        e.preventDefault();
        var id = window.location.href.split('=')[1];
        createPullRequest($('#target-branch').val(),$('#base-branch').val(),$("#pr-create-message").val());
        // $.ajax({
        //     type: $(this).attr('method'),
        //     url: $(this).attr('action'),
        //     data: {branchName: $("#branch-name",this).val(), id: id},
        //     timeout: 2000,
        //     error: function (a) {
        //         $('.modal-body-error').text(a.responseText);
        //         $('#error-modal').modal('show');
//
        //     },
        //     success: function(a) {
        //         createPullRequest($.parseJSON(a));
        //         $('#create-branch-modal').modal('hide');
        //     }
        // })
    });

    $("#push").click(push);
    $("#pull").click(pull);
    $("#manage-wc-commit").click(loadUpdateWcCommit);
    $("#commit").click(commit);
    $("#back").click(function () {
        window.location.href = "../mainScreen/mainScreen.html";
        showRepositoriesPage();
    });
});
var numOfBranches;
function hideRemoteRepositoryRelatedButtons(){
    $('#create-pr').hide();
    $('#push').hide();
    $('#pull').hide();
}
function getRepositoryInfo() {
    var repositoryDetails;
    var id = window.location.href.split('=')[1];
    var REPO_DETAILS_URL = buildUrlWithContextPath("repodetails");
    $.ajax( {
        type: 'GET',
        data: {
            'id' : id
        },
        url: REPO_DETAILS_URL,
        timeout: 2000,
        error : function () {},
        success: function (a) {
            //"{branchesNum=2, commitMessage=changed Foo PSVM to say hello to tao tao, activeBranch=master, name=rep 1, commitDate=Sun Jun 09 20:25:10 IDT 2019}{"HEAD":{"mBranchName":"master","mPointedCommitSha1":{"mSha1Code":"9e10ad75f3f2b5eea8ab9ba42263e742239ffc4e"},"mIsRemote":false,"mTracking":false},"test":{"mBranchName":"test","mPointedCommitSha1":{"mSha1Code":"013855ca533c572d3a29940f08048aa1ea8823ff"},"mIsRemote":false,"mTracking":false},"master":{"mBranchName":"master","mPointedCommitSha1":{"mSha1Code":"9e10ad75f3f2b5eea8ab9ba42263e742239ffc4e"},"mIsRemote":false,"mTracking":false}}"
            repositoryDetails = a;
            if(repositoryDetails.Repository.remoteId === "none"){
                hideRemoteRepositoryRelatedButtons();
            }
            numOfBranches = repositoryDetails.Repository.branchesNum;
            $(".card-repo").remove();
            $(".row-title").prepend(
                "<div class='m-2 col-xl-5 col-sm-5 square card card-repo' style='width: 50rem;background: rgba(202,255,240,0.74);'>" +
                "<div class='card-body'>" +
                "<h4 class='card-title'>" + repositoryDetails.Repository.name + "</h4>" +
                "<h6 class='card-subtitle mb-2 text-muted branches-count'>Number of Branches: " + $(".card-branch").length + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted head-title'>Head Branch: " + repositoryDetails.Repository.activeBranch + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Date: " + repositoryDetails.Repository.commitDate + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Message: " + repositoryDetails.Repository.commitMessage + "</h6>" +
                (repositoryDetails.Repository.remoteId === "none" ? "" :
                    "<h6 class='card-subtitle mb-2 text-muted'>Remote name: " + repositoryDetails.Repository.remoteName + "</h6>" +
                    "<h6 class='card-subtitle mb-2 text-muted'>Remote user: " + repositoryDetails.Repository.remoteUser + "</h6>") +
                "</div>" +
                "<div id="+ repositoryDetails.Repository.id + " class='remoteId'></div>" +
                "</div>");
            delete repositoryDetails.Repository;
            $(".branches-container").empty();
            for (var k in repositoryDetails) {
                $(".branches-container").append(
                    "<div class='card card-branch col-lg-3 col-sm-12 col-md-12' style='background: rgba(255,196,157,0.74);'>\n" +
                    "   <div class=\"container\"><!--change-->\n" +
                    "      <div class=\"row\"><!--change-->" +
                    "         <div class='col-lg-8 align-self-start card-body'>\n" +
                    "            <h4 class='card-title'>Branch Name: " + k + "</h4>\n" +
                    "            <h6 class='card-subtitle mb-2 text-muted'>Pointing Commit: " + repositoryDetails[k].Commit+ "</h6>\n" +
                    "            <h6 class='card-subtitle mb-2 text-muted'>Is Tracking: " + repositoryDetails[k].IsTracking + "</h6>\n" +
                    "            <h6 class='card-subtitle mb-2 text-muted'>Is Remote: " + repositoryDetails[k].IsRemote + "</h6>\n" +
                    "            <h6 class='card-subtitle mb-2 text-muted'>Tracking After: " + repositoryDetails[k].TrackingAfter + "</h6>\n" +
                    "         </div>\n" +
                    "         <div class='col-lg-4 align-self-center buttons-column'>\n" +
                    "            <button type='button' class='btn btn-branch delete-btn btn-danger w-100 col align-self-end'>Delete Branch</button>\n" +
                    "            <div class='divider'></div>\n" +
                    "            <button type='button' class='btn btn-branch head-btn btn-info w-100 align-self-end'>Checkout as Head</button>\n" +
                    "         </div>\n" +
                    "      </div>\n" +
                    "   </div>\n" +
                    "</div>\n");
                $(".card-branch").last().attr('name', k);
                $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
            }
            $(".delete-btn").click(deleteBranch);
            $(".head-btn").click(changeHead);
        }
    });
}

function createBranchView(branchDetails) {
    $(".branches-container").append(
        "<div class='card card-branch col-lg-3 col-sm-12 col-md-12' style='background: rgba(255,196,157,0.74);'>\n" +
        "   <div class=\"container\"><!--change-->\n" +
        "      <div class=\"row\"><!--change-->" +
        "         <div class='col-lg-8 align-self-start card-body'>\n" +
        "            <h4 class='card-title'>Branch Name: " + branchDetails.Name + "</h4>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Pointing Commit: " + branchDetails.Commit+ "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Is Tracking: " + branchDetails.IsTracking + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Is Remote: " + branchDetails.IsRemote + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Tracking After: " + branchDetails.TrackingAfter + "</h6>\n" +
        "         </div>\n" +
        "         <div class='col-lg-4 align-self-center buttons-column'>\n" +
        "            <button type='button' class='btn btn-branch delete-btn btn-danger w-100 col align-self-end'>Delete Branch</button>\n" +
        "            <div class='divider'></div>\n" +
        "            <button type='button' class='btn btn-branch head-btn btn-info w-100 align-self-end'>Checkout as Head</button>\n" +
        "         </div>\n" +
        "      </div>\n" +
        "   </div>\n" +
        "</div>");
    $(".card-branch").last().attr('name', branchDetails.Name);
    $(".delete-btn").last().click(deleteBranch);
    $(".head-btn").last().click(changeHead);
    $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
}

function deleteBranch() {
    var deleteBranchUrl = buildUrlWithContextPath("deleteBranch");
    var id = window.location.href.split('=')[1];
    var branch = $(this);
    var branchName =  $(this).parent().parent().parent().parent().attr('name');
    $.ajax( {
        data: {
            name: branchName,
            id: id
        },
        timeout: 2000,
        method: 'POST',
        url: deleteBranchUrl,
        error : function (a) {
            $('#error-modal').modal('show');
            $('.modal-body-error').text(a.responseText);
        },
        success: function() {
            branch.parent().parent().parent().parent().remove();
            $(".branches-count").text("Number of Branches: " + $(".card-branch").length);
        }
    })
}

function changeHead() {
    emptyExtraContainerContentAndHide()
    var id = window.location.href.split('=')[1];
    var checkoutUrl = buildUrlWithContextPath("checkout");
    var branchName = $(this).parent().parent().parent().parent().attr('name');
    $.ajax( {
        data: {
            name: branchName,
            id: id,
            requestType: "switch-branch"
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            if (a.responseJSON.requestType === "remote-branch") {
                showCreateRemoteBranchModal(branchName,a.responseJSON.msg);
            }else if(a.responseJSON.requestType === "open-changes"){
                showForceChangeBranchModal(branchName, a.responseJSON.msg);
            }
            else {
                errorToast(a.responseJSON.msg,true)
            }
        },
        success: function(a) {
            successToast(a.msg,false);
            $(".head-title").text("Head Branch: " + branchName);
            getCommitsInfo();
        }
    })
}

function showForceChangeBranchModal(branchName,message) {
    $('#yes-no-modal').on('show.bs.modal', function (event) {
        // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
        // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
        var modal = $(this);
        modal.find('.modal-title').text('Are you sure?');
        modal.find('#body-label').text(message);
        modal.find('#generic-submit').off('click').click(branchName,forceChangeBranch);
    });
    $('#yes-no-modal').modal('show');
}
function showCreateRemoteBranchModal(branchName,message) {
    $('#yes-no-modal').on('show.bs.modal', function (event) {
        // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
        // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
        var modal = $(this);
        modal.find('.modal-title').text('Remote tracking branch creation');
        modal.find('#body-label').text(message);
        modal.find('#generic-submit').off('click').click(branchName,createRTB);
    });
    $('#yes-no-modal').modal('show');
}
function forceChangeBranch(branchName) {
    var id = window.location.href.split('=')[1];
    var checkoutUrl = buildUrlWithContextPath("checkout");
    $.ajax( {
        data: {
            name: branchName.data,
            id: id,
            requestType: "force-checkout"
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            errorToast(a.responseJSON.msg,true)
        },
        success: function(a) {
            successToast(a.msg,false);
            $(".head-title").text("Head Branch: " + branchName.data);
            $('#yes-no-modal').modal('hide');
            getCommitsInfo();
        }
    })
}
function createRTB(branchName) {
    var id = window.location.href.split('=')[1];
    var checkoutUrl = buildUrlWithContextPath("checkout");
    $.ajax( {
        data: {
            name: branchName.data,
            id: id,
            requestType: "create-rtb"
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            errorToast(a.responseJSON.msg,true);
        },
        success: function(a) {
            successToast(a.msg,false);
            $(".head-title").text("Head Branch: " + branchName.data);
            $('#yes-no-modal').modal('hide');
            getRepositoryInfo()
            getCommitsInfo();
        }
    })
}
function createBranch() {
    $('#create-branch-modal').modal('show');
}
function createPr() {
    $('#create-pr-modal').modal('show');
}

function getCommitsInfo() {
    emptyExtraContainerContentAndHide()
    $(".side-container").empty();
    $.ajax({
            url: buildUrlWithContextPath("pages/repositoryDetails/commitsInfo"),
            data: {
                'id': window.location.href.split('=')[1]
            },
            type: 'GET',
            error: function (a) {
                errorToast(a.responseText);
            },
            success: function (commitsInfo) {
                var i = 0;
                commitsInfo = $.parseJSON(commitsInfo);
                $(".side-container").append(
                    "<table class='table table-hover'>" +
                    "<thead class='thead-dark'>" +
                    "<tr>" +
                    "<th scope='col'>#</th>" +
                    "<th scope='col'>Sha1</th>" +
                    "<th scope='col'>Creator</th>" +
                    "<th scope='col'>Message</th>" +
                    "<th scope='col'>Date</th>" +
                    "<th scope='col'>Pointed By</th>" +
                    "</tr>" +
                    "</thead>" +
                    "<tbody class='table-body'>" +
                    "</tbody>" +
                    "</table>");
                for (var key in commitsInfo) {
                    $(".table-body").append(
                        "<tr onclick='createTreeView(this, showCommit)' id=" + commitsInfo[key].Sha1 + ">" +
                        "<th scope='row'>" + (++i) + "</th>" +
                        "<td>" + commitsInfo[key].Sha1 + "</td>" +
                        "<td>" + commitsInfo[key].Creator + "</td>" +
                        "<td>" + commitsInfo[key].Message + "</td>" +
                        "<td>" + commitsInfo[key].Date + "</td>" +
                        "<td>" + commitsInfo[key].Branches + "</td>" +
                        "</tr>");
                }
            }
        }
    );
}

function createPullRequest(targetBranch, baseBranch, message) {
    $.ajax({
        url: buildUrlWithContextPath("pullrequest"),
        data:{
            'repository-id': window.location.href.split('=')[1],
            'request-id' : "none",
            'target-branch' : targetBranch,
            'base-branch' : baseBranch,
            'message' : message,
            'pr-action' : "pr-create"
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
        },
        success: function(msg) {
            successToast(msg,false,3000);
        }
    })
}

function showPullRequests() {
    emptyExtraContainerContentAndHide()
    $(".side-container").empty();
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("pullrequest"),
        data:{
            'repository-id': window.location.href.split('=')[1],
            'request-id' : 'none',
            'target-branch' : 'none',
            'base-branch' : 'none',
            'message' : "static for now",
            'pr-action' : "pr-show"
        },
        type: 'GET',
        error: function (prs) {},
        success: function(prs) {
            if(prs.length === 0){
                 noticeToast("There are no Pull requests to show", false, 3000);
            }else {
                $(".side-container").append($("<div class='container-fluid'><div class='row pull-requests pb-2'></div></div>"))
                $.each(prs || [], printPullRequest);
            }
        }
    })
}

function printPullRequest(id, pr) {
    pullRequest = $("<div class='card card-branch col-lg-2 col-sm-12 col-md-12 text-dark bg-warning'>\n" +
        "   <div class=\"container\"><!--change-->\n" +
        "      <div class=\"row\"><!--change-->" +
        "         <div class='col-lg-12 align-self-start card-body'>\n" +
        "            <h4 class='card-title'>From: " + pr.userName + "</h4>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Target branch: " + pr.targetBranch+ "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Base branch: " + pr.baseBranch + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Date: " + pr.date + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Request message: " + pr.message + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Status: <span><span class="+ (pr.status === "Open" ? "'badge badge-success'" : (pr.status === "Rejected" ? "'badge badge-danger'" : "'badge badge-secondary'")) + ">" + pr.status + "</span></span></h6>\n" +
        "         </div>\n" +
        "         <div class='col-lg-12 align-self-center buttons-column pb-2'>\n" +
        (pr.status === "Open" ? "<button type='button' class='btn btn-branch delete-btn btn-success w-100 col align-self-end acceptPullRequest' id="+ pr.requestId +">Accept PR</button>\n" +
            "            <div class='divider'></div>\n" +
            "            <button type='button' class='btn btn-branch head-btn btn-danger w-100 align-self-end rejectPullRequest' id="+ pr.requestId +">Reject PR</button>\n" +
            "            <div class='divider'></div>\n" +
            "            <button type='button' class='btn btn-branch head-btn btn-info w-100 align-self-end examinePullRequestChanges' id="+ pr.requestId +">Examine changes</button>\n" : "") +
        "         </div>\n" +
        "      </div>\n" +
        "   </div>\n" +
        "</div>");
    //{requestId:pr.requestId,targetBranch:pr.targetBranch,baseBranch:pr.baseBranch,message:pr.message}
    $('.acceptPullRequest',pullRequest).on('click',pr,acceptPullRequest);
    $('.rejectPullRequest',pullRequest).on('click',pr,rejectPullRequest);
    $('.examinePullRequestChanges',pullRequest).on('click',pr,examinePullRequestChanges);
    $(".pull-requests").append(pullRequest);
}

function acceptPullRequest(pr) {
    emptyExtraContainerContentAndHide();
    $(".side-container").empty();
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("pullrequest"),
        data:{
            'applicant' : pr.data.userName,
            'repository-id': window.location.href.split('=')[1],
            'request-id' : pr.data.requestId,
            'target-branch' : pr.data.targetBranch,
            'base-branch' : pr.data.baseBranch,
            'message' : pr.data.message,
            'pr-action' : "pr-accept"
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
            showPullRequests();
        },
        success: function(msg) {
            successToast(msg,false,3000);
            showPullRequests();
        }
    })
}
function rejectPullRequest(pr) {
    $('#reject-pr-message-modal').on('show.bs.modal', function (event) {
        // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
        // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
        var modal = $(this);
        var rejectMessage = modal.find('#reject-pr-message').val();
        modal.find('#reject-pr-message-send').off('click').click({pr: pr,modal :modal},sendRejectPullRequest);
    });
    $('#reject-pr-message-modal').modal('show');
}
function sendRejectPullRequest(params) {
    emptyExtraContainerContentAndHide();
    $(".side-container").empty();
    var rejectMessage = params.data.modal.find('#reject-pr-message').val();
    var pr = params.data.pr;
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("pullrequest"),
        data:{
            'applicant' : pr.data.userName,
            'repository-id': window.location.href.split('=')[1],
            'request-id' : pr.data.requestId,
            'target-branch' : pr.data.targetBranch,
            'base-branch' : pr.data.baseBranch,
            'message' : pr.data.message,
            'pr-action' : "pr-reject",
            'reject-message' : rejectMessage
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
            showPullRequests();
        },
        success: function(msg) {
            successToast(msg,false,3000);
            showPullRequests();
            $('#reject-pr-message-modal').modal('hide');
        }
    })
}

function examinePullRequestChanges(pr) {
    emptyExtraContainerContentAndHide();
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("pullrequest"),
        data:{
            'applicant' : pr.data.userName,
            'repository-id': window.location.href.split('=')[1],
            'request-id' : pr.data.requestId,
            'target-branch' : pr.data.targetBranch,
            'base-branch' : pr.data.baseBranch,
            'message' : pr.data.message,
            'pr-action' : "pr-diff"
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
            showPullRequests();
        },
        success: function(msg) {

            createTreeFromReadyJsTree(msg, showJsTreeFileInfo);
        }
    })
}

function createTreeFromReadyJsTree(jstreeArray, moreOptionsFunction) {
    $(".jstree-container").jstree('destroy');
    $('.extra-container','#repositories').css('display', 'block');
    $(".extra-container").append("<div class='jstree-container'></div>");
    $('.jstree-container').jstree({ 'core' : {
            'data' : jstreeArray
        } });
    $('.jstree').on('loaded.jstree', function(e, data) {
        // invoked after jstree has loaded
        $('.jstree').jstree('open_node', '#0');});
    moreOptionsFunction();
}

function showJsTreeFileInfo() {
    $('.jstree').on("select_node.jstree", function (e, data) {
        if(data.node.icon === 'jstree-folder') return;
        emptyTextAreaAtExtraContainer();
        addTextAreaWithContent(false, data.node)});

    $('.jstree').on("destroy.jstree", function () {
        emptyExtraContainerContentAndHide()});
}

function pull() {
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("collaboration"),
        data:{
            'action' : 'pull',
            'repository-id': window.location.href.split('=')[1],
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
        },
        success: function(msg) {
            successToast(msg,false, 3000);
        }
    })
}

function push() {
    $.ajax({
        type: $(this).attr('method'),
        url: buildUrlWithContextPath("collaboration"),
        data:{
            'action' : 'push',
            'repository-id': window.location.href.split('=')[1],
        },
        type: 'GET',
        error: function (err) {
            errorToast(err.responseText,false,3000);
        },
        success: function(msg) {
            successToast(msg,false, 3000);
        }
    })
}

function createTreeView(tableRow, moreOptionsFunction) {
    $(".jstree-container").jstree('destroy');
    $.ajax({
        url: buildUrlWithContextPath("createTreeView"),
        data: {
            'id': window.location.href.split('=')[1],
            'sha1': tableRow.id
        },
        type: 'GET',
        error : function() {
        },
        success: function(responseContent) {
            $('.extra-container','#repositories').css('display', 'block');
            $(".extra-container").append("<div class='jstree-container'></div>");
            $('.jstree-container').jstree({ 'core' : {
                    'data' : responseContent
                } });
            $('.jstree').on('loaded.jstree', function(e, data) {
                // invoked after jstree has loaded
                $('.jstree').jstree('open_node', '#0');});
            moreOptionsFunction(false);
        }
    })
}

function addTextAreaWithContent(isWcTree, node) {
    $('div.text-area', '.extra-container').empty();
    $('.extra-container').show();
    $('.extra-container').append("<div class='mt-2 pt-2 border-top text-area'><span>File Content</span><textarea cols=\"60\" rows=\"20\" class=\"form-control mb-2\" spellcheck=\"false\"></textarea></div>");
    $('textarea','.text-area').prop('readonly', true);
    $('textarea','.text-area').append(node.li_attr['content']);
    $('textarea','.text-area').numberedtextarea();
    $('textarea','.text-area').attr('path', node.li_attr['path']).attr('nodeId', node.id);
    if (isWcTree) {
        $(".extra-container").append(
            "<button id='content-save-btn' class='col-lg-1 btn btn-success'>" +
            "<i class='fas fa-save fa-2x'></i>" +
            "<i>      Save</i></button>" +
            "<button id='content-edit-btn' class='col-lg-1 btn btn-secondary'>" +
            "<i class='fas fa-edit fa-2x'></i>" +
            "<i>Edit</i></button>");
        $("#content-edit-btn").click(function() {
            $('textarea','.text-area').prop('readonly', false);
        });
        $("#content-save-btn").click(saveContent);
    }
}


function showCommit(isWcTree) {
    $('.jstree').on("select_node.jstree", function (e, data) {
        if(data.node.icon === 'jstree-folder') return;
        emptyTextAreaAtExtraContainer();
        addTextAreaWithContent(isWcTree, data.node)
    });

    $('.jstree').on("destroy.jstree", function () {
        emptyExtraContainerContentAndHide()});
}

function emptyTextAreaAtExtraContainer() {
    $('div.text-area','.extra-container').remove();
    $('.extra-container').hide();
    $("#content-edit-btn").remove();
    $("#content-save-btn").remove();
}

function emptyExtraContainerContentAndHide() {
    $('.extra-container').empty();
    $('.extra-container').hide();
}

function loadUpdateWcCommit() {
    $('.side-container').empty();
    $('.side-container').append('<div class="pb-2"><button type="button" class="btn btn-dark" onclick="loadUpdateWcCommit()">\n' +
        '                                    <i class="fas fa-folder fa-3x"></i>\n' +
        '                                    <i>Update working copy</i>\n' +
        '                                </button>\n' +
        '                                <button type="button" class="btn btn-success btn-bar" onclick="showOpenChangesTree()">\n' +
        '                                    <i class="fas fa-folder-plus fa-3x"></i>\n' +
        '                                    <i>Open changes tree</i>\n' +
        '                                </button></div>')
    $('.extra-container').empty();
    $(".jstree-container").jstree('destroy');
    $.ajax( {
        url: buildUrlWithContextPath("createWcView"),
        type: 'GET',
        data : {
            'id': window.location.href.split('=')[1]
        },
        error : function () {},
        success: function (responseContent) {
            $(".extra-container",'#repositories').css('display', 'block');
            $(".extra-container").append("<div class='jstree-container'></div>").show();
            $('.jstree-container').jstree({
                'core': {
                    'check_callback': true,
                    'data': responseContent
                },
                'plugins' : ["contextmenu"], contextmenu: {items: getContextMenuLayout}
            }).on('loaded.jstree', function (e, data) {
                // invoked after jstree has loaded
                $('.jstree').jstree('open_node', '#0');
            });
            showCommit(true);
        }
    });
}

function showOpenChangesTree() {
    $.ajax( {
        url: buildUrlWithContextPath("openchanges"),
        type: 'POST',
        data : {
            'id': window.location.href.split('=')[1]
        },
        error : function () {},
        success: function (responseContent) {
            createTreeFromReadyJsTree(responseContent, showJsTreeFileInfo);
        }
    });
}

function getContextMenuLayout(node) {
    var tree = $(".jstree").jstree(true);
    var items = {
        createItem : {
            label: "New File",
            action: function() {
                createFile(tree,node);
            }
        },
        createDir: {
            label: "New Folder",
            action: function() {
                createFolder(tree,node);
            }
        },
        renameItem : {
            label : "Rename",
            action : function() {
                renameFile(tree,node);
            }
        },
        deleteItem : {
            label: "Delete",
            action: function() {
                deleteFile(tree, node);
            }
        }
    };
    if (node.icon === "jstree-file") {
        delete items.createItem;
        delete items.createDir;
    }
    if (node.parent === "#") {
        delete items.deleteItem;
        delete items.renameItem;
    }

    return items;
}

function deleteFile(tree, node) {
    $('div.text-area').remove();
    tree.delete_node(node);
    $.ajax ({
        url: buildUrlWithContextPath("deleteFile"),
        data: {
            'id' : window.location.href.split('=')[1],
            'path': node.li_attr['path']
        },
        type: 'POST'
    });
}

function renameFile(tree, node) {
    var previousName = node.text;
    tree.edit(node, previousName, function () {
        var path = node.li_attr['path'];
        var lastPartIndex = path.lastIndexOf('\\');
        var newPath = path.substring(0, lastPartIndex) + "\\" + node.text;
        $.ajax({
            url: buildUrlWithContextPath("renameFile"),
            data: {
                'id': window.location.href.split('=')[1],
                'path': path,
                'newFileName': newPath
            },
            type: 'POST',
            error: function () {
                errorToast("Invalid file name", false, 3000);
                tree.set_text(node, previousName);
            },
            success: function () {
                node.li_attr['path'] = newPath
            }
        });
    });
}

function createFile(tree, node) {
    var newNodeId = tree.create_node(node, {icon: "jstree-file"});
    var child = tree.get_node(newNodeId);
    tree.edit(child, "",function() {
        var path = node.li_attr['path'] + "\\" + child.text;
        $.ajax ({
            url: buildUrlWithContextPath("createFile"),
            data: {
                'id' : window.location.href.split('=')[1],
                'path': path
            },
            type: 'POST',
            error : function() {
                errorToast("Couldn't create file", false, 3000);
                tree.delete_node(child);
            },
            success: function() {
                child.li_attr['path'] = path;
                child.li_attr['content'] =  "";
            }
        });
    });
}

function createFolder(tree, node) {
    var newNodeId = tree.create_node(node, {icon: "jstree-folder"});
    var child = tree.get_node(newNodeId);
    tree.edit(child, "",function() {
        var path = node.li_attr['path'] + "\\" + child.text;
        $.ajax ({
            url: buildUrlWithContextPath("createFolder"),
            data: {
                'id' : window.location.href.split('=')[1],
                'path': path
            },
            type: 'POST',
            success: function() {
                child.li_attr['path'] = path;
            }
        });
    });
}

function saveContent() {
    var nodeId = $('textarea','.text-area').attr('nodeId');
    var value = $('textarea','.text-area').val();
    var node = $(".jstree").jstree(true).get_node(nodeId);
    $.ajax( {
        url: buildUrlWithContextPath('saveContent'),
        data: {
            'id' : window.location.href.split('=')[1],
            'path' : node.li_attr['path'],
            'data' : value
        },
        type: 'POST',
        success : function() {
            node.li_attr['content'] = value;
            successToast("File saved!", false,3000);
            $('textarea','.text-area').prop('readonly', true);
        }
    });
}

function commit() {
    $('#commit-message-modal').modal('show');
}