//
//  BasicAudioMain.swift
//  APIExample
//
//  Created by ADMIN on 2020/5/18.
//  Copyright © 2020 Agora Corp. All rights reserved.
//

import UIKit
import AgoraRTE
import AGEVideoLayout
class BasicAudioEntry : UIViewController
{
    @IBOutlet weak var joinButton: AGButton!
    @IBOutlet weak var channelTextField: AGTextField!
    let identifier = "BasicAudio"
    
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

class BasicAudioMain: BaseViewController {
    var agoraKit: AgoraRteSdk!
    var scene: AgoraRteSceneProtocol!
    var microphoneTrack: AgoraRteMicrophoneAudioTrackProtocol!
    @IBOutlet weak var container: AGEVideoContainer!
    @IBOutlet weak var recordingVolumeSlider: UISlider!
    @IBOutlet weak var playbackVolumeSlider: UISlider!
    @IBOutlet weak var inEarMonitoringSwitch: UISwitch!
    var audioViews: [UInt:VideoView] = [:]
    let LOCAL_STREAM_ID = String(UInt.random(in: 1000...2000))
    
    
    // indicate if current instance has joined channel
    var isJoined: Bool = false
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        // get channel name from configs
        guard let channelName = configs["channelName"] as? String
            else { return }
        
        // layout render view
        recordingVolumeSlider.maximumValue = 400
        recordingVolumeSlider.minimumValue = 0
        recordingVolumeSlider.integerValue = 100
        
        playbackVolumeSlider.maximumValue = 400
        playbackVolumeSlider.minimumValue = 0
        playbackVolumeSlider.integerValue = 100
        
        let view = Bundle.loadVideoView(type: .local, audioOnly: true)
        view.uid = 0
        view.setPlaceholder(text: getAudioLabel(uid: 0, isLocal: true))
        audioViews[0] = view
        
        // initialize sdk
        
        // initialize media control
        
        // audio
        
        //initilize streaming control

    }
    
    override func willMove(toParent parent: UIViewController?) {
        if parent == nil {
            // leave channel when exiting the view
            if isJoined {
                scene.leave()
            }
        }
    }
    
    func sortedViews() -> [VideoView] {
        return Array(audioViews.values).sorted(by: { $0.uid < $1.uid })
    }
    
    @IBAction func onChangeRecordingVolume(_ sender:UISlider){
        let value:Int = Int(sender.value)
        print("adjustRecordingSignalVolume \(value)")
        microphoneTrack.adjustPublishVolume(value)
    }
    
    @IBAction func onChangePlaybackVolume(_ sender:UISlider){
        let value:Int = Int(sender.value)
        print("adjustPlaybackSignalVolume \(value)")
        microphoneTrack.adjustPlayoutVolume(value)
    }
    
    @IBAction func toggleInEarMonitoring(_ sender:UISwitch){
//        enum EAR_MONITORING_FILTER_TYPE {
//          /**
//           * 1: Do not add an audio filter to the in-ear monitor.
//           */
//          EAR_MONITORING_FILTER_NONE = (1<<0),
//          /**
//           * 2: Enable audio filters to the in-ear monitor.
//           */
//          EAR_MONITORING_FILTER_BUILT_IN_AUDIO_FILTERS = (1<<1),
//          /**
//           * 4: Enable noise suppression to the in-ear monitor.
//           */
//          EAR_MONITORING_FILTER_NOISE_SUPPRESSION = (1<<2)
//        };
        microphoneTrack.enableEarMonitor(sender.isOn, audioFilters: 1)
    }
}

extension BasicAudioMain: AgoraRteSceneDelegate {
    
}
