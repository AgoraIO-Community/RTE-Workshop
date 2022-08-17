//
//  ViewController.swift
//  MultiScreen
//
//  Created by zhaoyongqiang on 2022/8/8.
//

import UIKit
import AgoraRtcKit
import ReplayKit

class MultiScreenShareController: UIViewController {
    private lazy var canvasView: CanvasContainerView = {
        let view = CanvasContainerView()
        return view
    }()
    private lazy var toolView: ToolView = {
        let view = ToolView()
        return view
    }()
    private var agoraKit: AgoraRtcEngineKit?
    private lazy var rtcEngineConfig: AgoraRtcEngineConfig = {
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        config.channelProfile = .liveBroadcasting
        config.areaCode = .global
        return config
    }()
    private lazy var channelMediaOptions: AgoraRtcChannelMediaOptions = {
        let option = AgoraRtcChannelMediaOptions()
        option.autoSubscribeAudio = .of(true)
        option.autoSubscribeVideo = .of(true)
        option.clientRoleType = .of((Int32)(AgoraClientRole.broadcaster.rawValue))
        option.publishMicrophoneTrack = .of(true)
        option.publishCameraTrack = .of(true)
        option.publishCustomVideoTrack = .of(false)
        return option
    }()
    private lazy var screenChannelMediaOptions: AgoraRtcChannelMediaOptions = {
        let option = AgoraRtcChannelMediaOptions()
        option.autoSubscribeAudio = .of(false)
        option.autoSubscribeVideo = .of(false)
        option.clientRoleType = .of((Int32)(AgoraClientRole.broadcaster.rawValue))
        option.publishMicrophoneTrack = .of(false)
        option.publishCameraTrack = .of(false)
        option.publishCustomVideoTrack = .of(true)
        return option
    }()
        
    private var systemBroadcastPicker: RPSystemBroadcastPickerView?
    private var dataArray: [UserModel] = []
    private var channelName = "multiScreen"
    private let screenUid: UInt = 1000
    private let maxCount: Int = 8
    
    init(channelName: String) {
        super.init(nibName: nil, bundle: nil)
        self.channelName = channelName
        title = channelName
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupUI()
        setupAgoraKit()
        UIApplication.shared.isIdleTimerDisabled = true
        navigationItem.leftBarButtonItem = UIBarButtonItem()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationController?.interactivePopGestureRecognizer?.isEnabled = false
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        leaveChannel(uid: UserInfo.userId)
        leaveChannel(uid: UserInfo.userId + screenUid)
    }

    private func setupAgoraKit() {
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: rtcEngineConfig, delegate: self)
        agoraKit?.setClientRole(.broadcaster)
        agoraKit?.setChannelProfile(.liveBroadcasting)
        agoraKit?.enableVideo()
        agoraKit?.enableAudio()
        /// 开启扬声器
        agoraKit?.setDefaultAudioRouteToSpeakerphone(true)
        
        joinChannel(uid: UserInfo.userId, mediaOption: channelMediaOptions)
    }
    
    private func joinChannel(uid: UInt, mediaOption: AgoraRtcChannelMediaOptions) {
        let connection = AgoraRtcConnection()
        connection.channelId = channelName
        connection.localUid = uid
        let result = agoraKit?.joinChannelEx(byToken: KeyCenter.Token,
                                             connection: connection,
                                             delegate: self,
                                             mediaOptions: mediaOption,
                                             joinSuccess: nil)
        guard result == 0 else { return }
        print("join success")
        if let model = dataArray.first(where: { $0.uid == uid - screenUid}) {
            model.screenUid = uid
            model.isScreen = true
        } else {
            dataArray.append(UserModel())
        }
        canvasView.setModels(data: dataArray)
    }
    
    private func prepareSystemBroadcaster() {
        if #available(iOS 12.0, *) {
            let frame = CGRect(x: 0, y:0, width: 60, height: 60)
            systemBroadcastPicker = RPSystemBroadcastPickerView(frame: frame)
            systemBroadcastPicker?.showsMicrophoneButton = false
            systemBroadcastPicker?.autoresizingMask = [.flexibleTopMargin, .flexibleRightMargin]
            let bundleId = Bundle.main.bundleIdentifier ?? ""
            systemBroadcastPicker?.preferredExtension = "\(bundleId).Agora-ScreenShare-Extension";
            
        } else {
            print("Minimum support iOS version is 12.0")
        }
    }
    
    private func stopScreenCapture() {
        agoraKit?.stopScreenCapture()
        screenChannelMediaOptions.publishCustomVideoTrack = .of(false)
        agoraKit?.updateChannel(with: screenChannelMediaOptions)
        leaveChannel(uid: UserInfo.userId + screenUid)
    }
    private func startScreenCapture() {
        prepareSystemBroadcaster()
        guard let picker = systemBroadcastPicker else { return }
        for view in picker.subviews where view is UIButton {
            (view as? UIButton)?.sendActions(for: .allEvents)
            break
        }
        let params = AgoraScreenCaptureParameters2()
        params.captureVideo = true
        params.captureAudio = true
        let audioParams = AgoraScreenAudioParameters()
        audioParams.captureSignalVolume = 50
        params.audioParams = audioParams
        let videoParams = AgoraScreenVideoParameters()
        videoParams.frameRate = .fps30
        videoParams.bitrate = AgoraVideoBitrateStandard
        agoraKit?.startScreenCapture(params)
    }
    private func leaveChannel(uid: UInt) {
        let connection = AgoraRtcConnection()
        connection.channelId = channelName
        connection.localUid = uid
        agoraKit?.leaveChannelEx(connection, leaveChannelBlock: { state in
            print("leave channel: \(state)")
        })
    }
    
    private func setupUI() {
        view.addSubview(canvasView)
        view.addSubview(toolView)
        canvasView.translatesAutoresizingMaskIntoConstraints = false
        toolView.translatesAutoresizingMaskIntoConstraints = false
        
        canvasView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        canvasView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        canvasView.topAnchor.constraint(equalTo: view.topAnchor, constant: 75).isActive = true
        let h = Screen.height - 75 - (Screen.safeAreaBottomHeight() + 20) - 50 - 20
        canvasView.heightAnchor.constraint(equalToConstant: h).isActive = true
        
        toolView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        toolView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -10).isActive = true
        toolView.heightAnchor.constraint(equalToConstant: 50).isActive = true
        toolView.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -(Screen.safeAreaBottomHeight() + 20)).isActive = true
        
        canvasView.setupCanvasClosure = { [weak self] model, isLocal in
            guard let self = self else { return }
            let canvas = AgoraRtcVideoCanvas()
            canvas.renderMode = .hidden
            canvas.uid = model.uid
            canvas.view = model.canvasView
            let connection = AgoraRtcConnection()
            connection.localUid = UserInfo.userId
            connection.channelId = self.channelName
            if model.isScreen {
                let screenCanvas = AgoraRtcVideoCanvas()
                screenCanvas.uid = model.screenUid
                screenCanvas.view = model.screenCanvasView
                screenCanvas.renderMode = .fit
                self.agoraKit?.setupRemoteVideoEx(screenCanvas, connection: connection)
            }
            let _ = isLocal ? self.agoraKit?.setupLocalVideo(canvas) : self.agoraKit?.setupRemoteVideoEx(canvas, connection: connection)
            self.agoraKit?.startPreview()
        }
        
        toolView.onTapToolButtonClosure = { [weak self] type, isSelected in
            guard let self = self else { return }
            switch type {
            case .close:
                self.navigationController?.popViewController(animated: true)
                
            case .screen:
                isSelected ? self.stopScreenCapture() : self.startScreenCapture()
                
            case .mic:
                self.channelMediaOptions.publishMicrophoneTrack = .of(isSelected)
                self.agoraKit?.updateChannel(with: self.channelMediaOptions)
                _ = isSelected ? self.agoraKit?.disableAudio() : self.agoraKit?.enableAudio()
                
            case .video:
                self.channelMediaOptions.publishCameraTrack = .of(isSelected)
                self.agoraKit?.updateChannel(with: self.channelMediaOptions)
                _ = isSelected ? self.agoraKit?.disableVideo() : self.agoraKit?.enableVideo()
            }
        }
    }
}

extension MultiScreenShareController: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        print("warning: \(warningCode.rawValue)")
    }
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        print("error: \(errorCode)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        print("Join \(channel) with uid \(uid) elapsed \(elapsed)ms")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        print("remote user join: \(uid) \(elapsed)ms")
        if dataArray.count >= maxCount { return }
        let userId = uid > screenUid ? uid - screenUid : uid
        if uid > screenUid {
            let model = dataArray.first(where: { $0.uid == userId })
            if model == nil {
                let model = UserModel()
                model.uid = userId
                model.screenUid = uid
                model.isScreen = true
                dataArray.append(model)
            } else {
                model?.isScreen = true
                model?.screenUid = uid
            }
        } else {
            if dataArray.contains(where: { $0.uid == userId }) { return }
            let model = UserModel()
            model.uid = uid
            dataArray.append(model)
        }
        canvasView.setModels(data: dataArray)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        print("remote user leval: \(uid) reason \(reason)")
        let userId = uid > screenUid ? uid - screenUid : uid
        if uid > screenUid, let model = dataArray.first(where: { $0.uid == userId }) {
            model.screenUid = 0
            model.isScreen = false
        } else if let index = dataArray.firstIndex(where: { $0.uid == userId }) {
            dataArray.remove(at: index)
        }
        canvasView.setModels(data: dataArray)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStateChangedOf state: AgoraVideoLocalState, error: AgoraLocalVideoStreamError, sourceType: AgoraVideoSourceType) {
        
        switch error {
        case .extensionCaptureStarted:
            print("屏幕共享开始")
            screenChannelMediaOptions.publishCustomVideoTrack = .of(true)
            joinChannel(uid: UserInfo.userId + screenUid, mediaOption: screenChannelMediaOptions)
            toolView.updateScreenButtonStatus(isSelected: true)
            
        case .extensionCaptureStoped:
            print("屏幕共享停止")
            toolView.updateScreenButtonStatus(isSelected: false)

        case .extensionCaptureDisconnected:
            print("断开连接")
            toolView.updateScreenButtonStatus(isSelected: false)
        
        default: break
        }
    }
}
