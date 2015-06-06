angular.module(PKG.name + '.feature.datasets')
  .controller('CdapDatasetMetadataController',
    function($scope, $state, myExploreApi) {

      var datasetId = $state.params.datasetId;
      datasetId = datasetId.replace(/[\.\-]/g, '_');

      var params = {
        namespace: $state.params.namespace,
        table: 'dataset_' + datasetId,
        scope: $scope
      };

      myExploreApi.getInfo(params)
        .$promise
        .then(function (res) {
          $scope.metadata = res;
        });

    }
  );
