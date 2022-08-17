//
//  JoinViewController.swift
//  MultiScreen
//
//  Created by zhaoyongqiang on 2022/8/9.
//

import UIKit
import Agora_Scene_Utils

class JoinViewController: UIViewController {
    private lazy var containerView: AGEView = {
        let view = AGEView()
        return view
    }()
    private lazy var imageView: AGEImageView = {
        let imageView = AGEImageView(imageName: "logo")
        imageView.contentMode = .scaleAspectFill
        return imageView
    }()
    private lazy var titleLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .large)
        label.numberOfLines = 0
        label.textAlignment = .center
        label.text = "欢迎使用\n\n声网4.0.0 SDK"
        return label
    }()
    private lazy var textField: AGETextField = {
        let textField = AGETextField(colorStyle: .black, fontStyle: .middle)
        textField.placeholder = "请输入频道名"
        textField.addTarget(self, action: #selector(textfieldValueChange(sender:)), for: .editingChanged)
        return textField
    }()
    private lazy var joinButton: AGEButton = {
        let button = AGEButton(style: .filled(backgroundColor: UIColor(hex: "#a4adb3")))
        button.setTitle("加  入", for: .normal)
        button.cornerRadius = 5
        button.addTarget(self, action: #selector(onTapJoinButton), for: .touchUpInside)
        return button
    }()
    
    private var containerViewbottomCons: NSLayoutConstraint?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Workshop"
        view.addSubview(containerView)
        containerView.addSubview(imageView)
        containerView.addSubview(titleLabel)
        containerView.addSubview(textField)
        containerView.addSubview(joinButton)
        containerView.translatesAutoresizingMaskIntoConstraints = false
        imageView.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        textField.translatesAutoresizingMaskIntoConstraints = false
        joinButton.translatesAutoresizingMaskIntoConstraints = false
        
        containerView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        containerView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        containerView.heightAnchor.constraint(equalTo: view.heightAnchor).isActive = true
        containerViewbottomCons = containerView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        
        imageView.topAnchor.constraint(equalTo: containerView.topAnchor, constant: Screen.kNavHeight).isActive = true
        imageView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor, constant: 1).isActive = true
        imageView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor, constant: -1).isActive = true
        imageView.heightAnchor.constraint(equalToConstant: 140).isActive = true
        
        titleLabel.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
        titleLabel.centerYAnchor.constraint(equalTo: containerView.centerYAnchor, constant: -40).isActive = true
        
        textField.topAnchor.constraint(equalTo: titleLabel.topAnchor, constant: 155).isActive = true
        textField.leadingAnchor.constraint(equalTo: containerView.leadingAnchor, constant: 45).isActive = true
        textField.trailingAnchor.constraint(equalTo: containerView.trailingAnchor, constant: -45).isActive = true
        textField.heightAnchor.constraint(equalToConstant: 40).isActive = true
        
        joinButton.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
        joinButton.topAnchor.constraint(equalTo: textField.bottomAnchor, constant: 30).isActive = true
        joinButton.widthAnchor.constraint(equalToConstant: 106).isActive = true
        joinButton.heightAnchor.constraint(equalToConstant: 30).isActive = true
        
        NotificationCenter.default.addObserver(self, selector: #selector(willShowKeyBoard(noti:)), name: UIApplication.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(willHiddenKeyBoard(noti:)), name: UIApplication.keyboardWillHideNotification, object: nil)
    }
    
    @objc
    private func onTapJoinButton() {
        textField.resignFirstResponder()
        guard let channelName = textField.text?.trimmingCharacters(in: .whitespacesAndNewlines), channelName.isEmpty == false else { return }
        let vc = MultiScreenShareController(channelName: channelName)
        navigationController?.pushViewController(vc, animated: true)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        textField.resignFirstResponder()
    }
    
    @objc
    private func textfieldValueChange(sender: UITextField) {
        joinButton.backgroundColor = (sender.text?.isEmpty ?? false) ? UIColor(hex: "#a4adb3") : UIColor(hex: "#1784fc")
    }
    
    @objc
    private func willShowKeyBoard(noti: Notification) {
        let y = (noti.userInfo?["UIKeyboardCenterBeginUserInfoKey"] as? CGPoint)?.x
        containerViewbottomCons?.constant = -(y ?? 0)
        containerViewbottomCons?.isActive = true
        UIView.animate(withDuration: 0.25) {
            self.view.layoutIfNeeded()
        }
    }
    @objc
    private func willHiddenKeyBoard(noti: Notification) {
        containerViewbottomCons?.constant = 0
        containerViewbottomCons?.isActive = true
        UIView.animate(withDuration: 0.25) {
            self.view.layoutIfNeeded()
        }
    }
}
