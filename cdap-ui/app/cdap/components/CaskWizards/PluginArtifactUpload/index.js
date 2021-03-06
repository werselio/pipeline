/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import React, {Component, PropTypes} from 'react';
import WizardModal from 'components/WizardModal';
import Wizard from 'components/Wizard';
import PluginArtifactUploadWizardConfig from 'services/WizardConfigs/PluginArtifactUploadWizardConfig';
import PluginArtifactUploadStore from 'services/WizardStores/PluginArtifactUpload/PluginArtifactUploadStore';
import PluginArtifactUploadActions from 'services/WizardStores/PluginArtifactUpload/PluginArtifactUploadActions';
import ArtifactUploadActionCreator from 'services/WizardStores/PluginArtifactUpload/ActionCreator';
import NamespaceStore from 'services/NamespaceStore';
import T from 'i18n-react';

require('./PluginArtifactUpload.scss');

export default class PluginArtifactUploadWizard extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showWizard: this.props.isOpen
    };
    this.successInfo = {};
  }
  componentWillUnmount() {
    PluginArtifactUploadStore.dispatch({
      type: PluginArtifactUploadActions.onReset
    });
  }
  onSubmit() {
    this.buildSuccessInfo();
    return ArtifactUploadActionCreator
      .uploadArtifact()
      .flatMap(() => ArtifactUploadActionCreator.uploadConfigurationJson());
  }
  toggleWizard(returnResult) {
    if (this.state.showWizard) {
      this.props.onClose(returnResult);
    }
    this.setState({
      showWizard: !this.state.showWizard
    });
  }
  buildSuccessInfo() {
    let state = PluginArtifactUploadStore.getState();
    let name = state.upload.jar.fileMetadataObj.name;
    let namespace = NamespaceStore.getState().selectedNamespace;
    let defaultSuccessMessage = T.translate('features.Wizard.PluginArtifact.success');
    let subtitle = T.translate('features.Wizard.PluginArtifact.subtitle');
    let buttonLabel = T.translate('features.Wizard.PluginArtifact.callToAction');
    let linkLabel = T.translate('features.Wizard.GoToHomePage');
    this.successInfo.message = `${defaultSuccessMessage} "${name}".`;
    this.successInfo.subtitle = subtitle;
    this.successInfo.buttonLabel = buttonLabel;
    this.successInfo.buttonUrl = `/hydrator/ns/${namespace}/studio`;
    this.successInfo.linkLabel = linkLabel;
    this.successInfo.linkUrl = `/cdap/ns/${namespace}`;
  }
  render() {
    let input = this.props.input;
    let pkg = input.package || {};
    let headerLabel = input.headerLabel;

    let wizardModalTitle = (pkg.label ? pkg.label + " | " : '') + (headerLabel ? headerLabel : T.translate('features.Wizard.Informational.headerlabel'));
    return (
      <WizardModal
        title={wizardModalTitle}
        isOpen={this.state.showWizard}
        toggle={this.toggleWizard.bind(this, false)}
        className="artifact-upload-wizard"
      >
        <Wizard
          wizardConfig={PluginArtifactUploadWizardConfig}
          wizardType="ArtifactUpload"
          store={PluginArtifactUploadStore}
          onSubmit={this.onSubmit.bind(this)}
          successInfo={this.successInfo}
          onClose={this.toggleWizard.bind(this)}/>
      </WizardModal>
    );
  }
}

PluginArtifactUploadWizard.defaultProps = {
  input: {
    action: {
      arguments: {}
    },
    package: {}
  }
};
PluginArtifactUploadWizard.propTypes = {
  isOpen: PropTypes.bool,
  input: PropTypes.any,
  onClose: PropTypes.func
};
