//
//  SimpleFilter.swift
//  APIExample
//
//  Created by ADMIN on 2020/5/18.
//  Copyright © 2020 Agora Corp. All rights reserved.
//

import UIKit
import AgoraRTE
import AGEVideoLayout
import SimpleFilter

class SimpleFilterEntry : UIViewController
{
    @IBOutlet weak var joinButton: AGButton!
    @IBOutlet weak var channelTextField: AGTextField!
    let identifier = "SimpleFilter"
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    @IBAction func doJoinPressed(sender: AGButton) {
        guard let channelName = channelTextField.text else {return}
        //resign channel text field
        channelTextField.resignFirstResponder()
        
        let storyBoard: UIStoryboard = UIStoryboard(name: identifier, bundle: nil)
        // create new view controller every time to ensure we get a clean vc
        guard let newViewController = storyBoard.instantiateViewController(withIdentifier: identifier) as? BaseViewController else {return}
        newViewController.title = channelName
        newViewController.configs = ["channelName":channelName]
        self.navigationController?.pushViewController(newViewController, animated: true)
    }
    
}

class SimpleFilterMain: BaseViewController {
    
    @IBOutlet weak var container: AGEVideoContainer!
    var localVideo = Bundle.loadVideoView(type: .local, audioOnly: false)
    var remoteVideo = Bundle.loadVideoView(type: .remote, audioOnly: false)
    let AUDIO_FILTER_NAME = "VolumeChange"
    let VIDEO_FILTER_NAME = "Watermark"
    
    var agoraKit: AgoraRteSdk!
    
    var scene: AgoraRteSceneProtocol!
    var microphoneTrack: AgoraRteMicrophoneAudioTrackProtocol!
    var cameraTrack: AgoraRteCameraVideoTrackProtocol!
    let LOCAL_USER_ID = String(UInt.random(in: 100...999))
    let LOCAL_STREAM_ID = String(UInt.random(in: 1000...2000))
    
    // indicate if current instance has joined channel
    var isJoined: Bool = false
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        // get channel name from configs
        guard let channelName = configs["channelName"] as? String
            else { return }
        
        // layout render view
        localVideo.setPlaceholder(text: "Local Host".localized)
        remoteVideo.setPlaceholder(text: "Remote Host".localized)
        container.layoutStream(views: [localVideo, remoteVideo])
        
        // initialize sdk
        
        // initialize media control
        
        // audio
        
        // video
        
        //initilize streaming control
    }
    
    override func willMove(toParent parent: UIViewController?) {
        if parent == nil {
            // leave channel when exiting the view
            if isJoined {
                scene?.leave()
            }
        }
    }
    
    @IBAction func onChangeRecordingVolume(_ sender:UISlider){
        let value:Int = Int(sender.value)
        print("adjustRecordingSignalVolume \(value)")
        microphoneTrack.setExtensionPropertyWithProviderName(SimpleFilterManager.vendorName(), extensionName: AUDIO_FILTER_NAME, key: "volume", jsonValue: String(value))
    }
}

extension SimpleFilterMain: AgoraRteSceneDelegate {
    
}

extension SimpleFilterMain: AgoraMediaFilterEventDelegate{
    func onEvent(_ vendor: String?, extension: String?, key: String?, json_value: String?) {
        
    }
}
