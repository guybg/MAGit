function getRepositoryDetails() {
    var id = window.location.href.split('=')[1];
    var repositoryDetails;
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
            repositoryDetails = a;
        }
    });

    
}

$(function() {
    getRepositoryDetails();
});
