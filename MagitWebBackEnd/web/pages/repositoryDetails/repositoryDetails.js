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
});
var numOfBranches;

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
            $(".side-container").append($("<div class='container-fluid'><div class='row pull-requests pb-2'></div></div>"))
            $.each(prs || [], printPullRequest);
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
            'pr-action' : "pr-reject"
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
        addTextAreaWithContent($("#"+data.node.id).attr("content"),true)});

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


/*function createTreeView() {
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
            buildTree($.parseJSON(responseContent));
        }
    })
}*/

//function buildTree(jsonContent) {
//    $(".side-container").empty();
//    var jsonTreeData = [];
//    var nodeQueue = [];
//    var id = 0;
//    nodeQueue.push({'node' : jsonContent, 'parent' : '#' });
//    while (nodeQueue.length > 0) {
//        var currentNodePair = nodeQueue.shift();
//        var jsonNode = { "id" : id,
//            "parent" : currentNodePair.parent, "text" : currentNodePair.node.mName, "icon" : "jstree-folder"};
//        if (typeof currentNodePair.node.mFiles === 'undefined'){
//            jsonNode.icon = "jstree-file";
//            jsonTreeData.push(jsonNode);
//            id++;
//            continue;
//        }
//        jsonTreeData.push(jsonNode);
//        for (var i = 0;i < currentNodePair.node.mFiles.length; i++) {
//            nodeQueue.push({ 'node': currentNodePair.node.mFiles[i], 'parent' : id});
//        }
//        id++;
//    }
//    $(".side-container").append("<div class='jstree-container'></div>");
//
//    $(".jstree-container").jstree( { 'core' : {
//            'data' : jsonTreeData
//        }});
//}

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
            moreOptionsFunction(true);
        }
    })
}

function addTextAreaWithContent(content,isReadOnly) {
    $('div.text-area', '.extra-container').empty();
    $('.extra-container').show();
    $('.extra-container').append("<div class='mt-2 pt-2 border-top text-area'><span>File Content</span><textarea cols=\"60\" rows=\"20\" class=\"form-control mb-2\" spellcheck=\"false\"></textarea></div>");
    $('textarea','.text-area').prop('readonly', isReadOnly);
    $('textarea','.text-area').append(content);
    $('textarea','.text-area').numberedtextarea();
}


function showCommit(isReadOnly) {
    $('.jstree').on("select_node.jstree", function (e, data) {
        if(data.node.icon === 'jstree-folder') return;
        emptyTextAreaAtExtraContainer();
        addTextAreaWithContent($("#"+data.node.id).attr("content"),isReadOnly)});

    $('.jstree').on("destroy.jstree", function () {
        emptyExtraContainerContentAndHide()});
}

function emptyTextAreaAtExtraContainer() {
    $('div.text-area','.extra-container').remove();
    $('.extra-container').hide();
}

function emptyExtraContainerContentAndHide() {
    $('.extra-container').empty();
    $('.extra-container').hide();
}

function loadUpdateWcCommit() {
    $('.side-container').empty();
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
            showCommit(false);
        }
    });
}

function getContextMenuLayout(node) {
    var tree = $(".jstree").jstree(true);
    var items = {
        createItem : {
            label: "New File",
            action: function() {
                var newNode = tree.create_node(node, {icon: "jstree-file"});
                tree.edit(newNode);
            }
        },
        createDir: {
            label: "New Folder",
            action: function() {
                var newFolder = tree.create_node(node, {icon: "jstree-folder"});
                tree.edit(newFolder);
            }
        },
        renameItem : {
            label : "Rename",
            action : function() {
                tree.edit(node);
            }
        },
        deleteItem : {
            label: "Delete",
            action: function() {
                tree.delete_node(node);
            }
        }
    };
    if (node.icon === "jstree-file") {
        delete items.createItem;
        delete items.createDir;
    }

    return items;
}
