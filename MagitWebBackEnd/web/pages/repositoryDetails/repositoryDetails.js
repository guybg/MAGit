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
            $(".row-title").prepend(
                "<div class='m-2 col-xl-5 col-sm-5 square card card-repo' style='width: 50rem;background: rgba(202,255,240,0.74);'>" +
                "<div class='card-body'>" +
                "<h4 class='card-title'>" + repositoryDetails.Repository.name + "</h4>" +
                "<h6 class='card-subtitle mb-2 text-muted branches-count'>Number of Branches: " + $(".card-branch").length + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted head-title'>Head Branch: " + repositoryDetails.Repository.activeBranch + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Date: " + repositoryDetails.Repository.commitDate + "</h6>" +
                "<h6 class='card-subtitle mb-2 text-muted'>Last Commit Message: " + repositoryDetails.Repository.commitMessage + "</h6>" +
                "</div>" +
                "<div id="+ repositoryDetails.Repository.id + " class='remote-id'></div>" +
                "</div>");
            delete repositoryDetails.Repository;
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
    var id = window.location.href.split('=')[1];
    var checkoutUrl = buildUrlWithContextPath("checkout");
    var branchName = $(this).parent().parent().parent().parent().attr('name');
    $.ajax( {
        data: {
            name: branchName,
            id: id
        },
        method: 'POST',
        url: checkoutUrl,
        error : function (a) {
            if (a.responseText.includes("checkout into a remote branch")) {
                errorToast("You are trying to checkout into a remote branch, this operation is forbidden." +
                    " Please checkout by using a remote tracking branch instead.",true);
            }
            else {
                errorToast(a.responseText,true)
            }
        },
        success: function() {
            $(".head-title").text("Head Branch: " + branchName);
            $(".side-container").empty();
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
    $.ajax({
            url: buildUrlWithContextPath("pages/repositoryDetails/commitsInfo"),
            data: {
                'id': window.location.href.split('=')[1]
            },
            type: 'GET',
            error: function (a) {

            },
            success: function (commitsInfo) {
                var i = 0;
                commitsInfo = $.parseJSON(commitsInfo);
                for (var key in commitsInfo) {
                    $(".table-body").append(
                        "<tr>" +
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
        error: function (a) {},
        success: function(a) {
            showPullRequests();
        }
    })
}

function showPullRequests() {
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
            $(".side-container").append($("<div class='container'><div class='row pull-requests'></div></div>"))
            $.each(prs || [], printPullRequest);
        }
    })
}

function printPullRequest(id, pr) {
    pullRequest = $("<div class='card card-branch col-lg-3 col-sm-12 col-md-12' style='background: rgba(255,196,157,0.74);'>\n" +
        "   <div class=\"container\"><!--change-->\n" +
        "      <div class=\"row\"><!--change-->" +
        "         <div class='col-lg-12 align-self-start card-body'>\n" +
        "            <h4 class='card-title'>From: " + pr.userName + "</h4>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Target branch: " + pr.targetBranch+ "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Base branch: " + pr.baseBranch + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Date: " + pr.date + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Request message: " + pr.message + "</h6>\n" +
        "            <h6 class='card-subtitle mb-2 text-muted'>Status: " + pr.status + "</h6>\n" +
        "         </div>\n" +
        "         <div class='col-lg-12 align-self-center buttons-column'>\n" +
        "            <button type='button' class='btn btn-branch delete-btn btn-info w-100 col align-self-end acceptPullRequest' id="+ pr.requestId +">Accept PR</button>\n" +
        "            <div class='divider'></div>\n" +
        "            <button type='button' class='btn btn-branch head-btn btn-danger w-100 align-self-end rejectPullRequest' id="+ pr.requestId +">Reject PR</button>\n" +
        "         </div>\n" +
        "      </div>\n" +
        "   </div>\n" +
        "</div>");
    //{requestId:pr.requestId,targetBranch:pr.targetBranch,baseBranch:pr.baseBranch,message:pr.message}
    var requestId = pr.requestId;
    var targetBranch = pr.targetBranch;
    var baseBranch = pr.baseBranch;
    var message = pr.message;
    $('.acceptPullRequest',pullRequest).on('click',pr,acceptPullRequest);
    $('.rejectPullRequest',pullRequest).on('click',pr,rejectPullRequest);
    $(".pull-requests").append(pullRequest);
}

function acceptPullRequest(pr) {
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
        error: function (prs) {},
        success: function(prs) {
            showPullRequests();
        }
    })
}

function rejectPullRequest(pr) {
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
        error: function (prs) {},
        success: function(prs) {
            showPullRequests();
        }
    })
}
