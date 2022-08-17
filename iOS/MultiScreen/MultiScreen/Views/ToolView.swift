//
//  ToolView.swift
//  MultiScreen
//
//  Created by zhaoyongqiang on 2022/8/8.
//

import UIKit
import Agora_Scene_Utils

enum BottomType: Int, CaseIterable {
    case close = 1
    case screen
    case mic
    case video
    
    var image: UIImage? {
        switch self {
        case .close: return UIImage(systemName: "xmark")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .screen: return UIImage(systemName: "sparkles.tv")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .mic: return UIImage(systemName: "mic")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .video: return UIImage(systemName: "video")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        }
    }
    var selectedImage: UIImage? {
        switch self {
        case .close: return UIImage(systemName: "xmark")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .screen: return UIImage(systemName: "sparkles.tv.fill")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .mic: return UIImage(systemName: "mic.slash")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        case .video: return UIImage(systemName: "video.slash")?.withTintColor(.white, renderingMode: .alwaysOriginal)
        }
    }
}

class ToolView: UIView {
    var onTapToolButtonClosure: ((BottomType, Bool) -> Void)?
    
    private lazy var statckView: UIStackView = {
        let stackView = UIStackView()
        stackView.alignment = .fill
        stackView.axis = .horizontal
        stackView.distribution = .fill
        stackView.spacing = 10
        return stackView
    }()
    private lazy var buttons: [AGEButton] = BottomType.allCases.reversed().map({
        let button = AGEButton()
        button.tag = $0.rawValue
        button.buttonStyle = .filled(backgroundColor: .black.withAlphaComponent(0.6))
        button.cornerRadius = 25
        button.setImage($0.image, for: .normal)
        button.setImage($0.selectedImage, for: .selected)
        button.widthAnchor.constraint(equalToConstant: 50).isActive = true
        button.heightAnchor.constraint(equalToConstant: 50).isActive = true
        button.addTarget(self, action: #selector(onTapButton(sender:)), for: .touchUpInside)
        return button
    })
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func updateScreenButtonStatus(isSelected: Bool) {
        let button = statckView.viewWithTag(BottomType.screen.rawValue) as? UIButton
        button?.isSelected = isSelected
    }
    
    private func setupUI() {
        addSubview(statckView)
        statckView.translatesAutoresizingMaskIntoConstraints = false
        statckView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        statckView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        statckView.heightAnchor.constraint(equalTo: heightAnchor).isActive = true
        
        buttons.forEach({
            statckView.addArrangedSubview($0)
        })
    }
    
    @objc
    private func onTapButton(sender: AGEButton) {
        let type = BottomType(rawValue: sender.tag) ?? .close
        if type != .screen {
            sender.isSelected = !sender.isSelected
        }
        onTapToolButtonClosure?(type, sender.isSelected)
    }
}
