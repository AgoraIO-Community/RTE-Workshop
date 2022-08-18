//
//  CanvasContainerView.swift
//  MultiScreen
//
//  Created by zhaoyongqiang on 2022/8/8.
//

import UIKit
import AgoraRtcKit
import Agora_Scene_Utils

class UserModel {
    var uid: UInt = UserInfo.userId
    var screenUid: UInt = 0
    var isScreen: Bool = false
    var screenCanvasView: UIView?
    var canvasView: UIView?
}

var itemH: CGFloat = 200
var itemW = (Screen.width - 30) / 2
class CanvasContainerView: UIView {
    /// 设置视频画面
    var setupCanvasClosure: ((UserModel, Bool) -> Void)?
    
    public lazy var collectionView: AGECollectionView = {
        let view = AGECollectionView()
        view.minInteritemSpacing = 10
        view.minLineSpacing = 10
        view.delegate = self
        view.edge = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
        view.scrollDirection = .vertical
        view.register(CanvasViewCell.self,
                      forCellWithReuseIdentifier: CanvasViewCell.description())
        return view
    }()
    private var dataArray: [UserModel] = [] {
        didSet {
            collectionView.dataArray = dataArray
            collectionView.reloadData()
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setModels(data: [UserModel]) {
        dataArray = data
    }
    
    private func setupUI() {
        addSubview(collectionView)
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        collectionView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        collectionView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        collectionView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }
}

extension CanvasContainerView: AGECollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: CanvasViewCell.description(),
                                                      for: indexPath) as! CanvasViewCell
        let model = dataArray[indexPath.item]
        cell.setupCanvasClosure = setupCanvasClosure
        cell.setCanvas(model: model, count: dataArray.count)
        return cell
    }
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        switch dataArray.count {
        case 1:
            itemW = Screen.width - 30
            itemH = Screen.height - 75 - (Screen.safeAreaBottomHeight() + 20) - 50 - 20
            return CGSize(width: itemW, height: itemH)
            
        case 2:
            itemW = Screen.width - 30
            itemH = 250
            return CGSize(width: itemW, height: itemH)
            
        default:
            itemW = (Screen.width - 30) / 2
            itemH = 200
            return CGSize(width: itemW, height: itemH)
        }
    }
}


class CanvasViewCell: UICollectionViewCell {
    /// 设置视频画面
    var setupCanvasClosure: ((UserModel, Bool) -> Void)?
    
    private lazy var screenCanvasView: AGEView = {
        let view = AGEView()
        view.backgroundColor = UIColor().randomColor
        return view
    }()
    private lazy var personCanvasView: PersonCanvasView = {
        let view = PersonCanvasView()
        view.layer.masksToBounds = true
        return view
    }()
    
    private var personLeadingCons: NSLayoutConstraint?
    private var personBottomCons: NSLayoutConstraint?
    private var personWidthCons: NSLayoutConstraint?
    private var personHeightCons: NSLayoutConstraint?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        contentView.layer.cornerRadius = 10
        contentView.layer.masksToBounds = true
        contentView.addSubview(screenCanvasView)
        contentView.addSubview(personCanvasView)
        screenCanvasView.translatesAutoresizingMaskIntoConstraints = false
        personCanvasView.translatesAutoresizingMaskIntoConstraints = false
        
        screenCanvasView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor).isActive = true
        screenCanvasView.topAnchor.constraint(equalTo: contentView.topAnchor).isActive = true
        screenCanvasView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor).isActive = true
        screenCanvasView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor).isActive = true
        
        personLeadingCons = personCanvasView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor)
        personBottomCons = personCanvasView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor)
        personWidthCons = personCanvasView.widthAnchor.constraint(equalToConstant: itemW)
        personHeightCons = personCanvasView.heightAnchor.constraint(equalToConstant: itemH)
    }
    
    func setCanvas(model: UserModel, count: Int) {
        if model.isScreen {
            personLeadingCons?.constant = 10
            personBottomCons?.constant = -10
            personWidthCons?.constant = count < 3 ? 80 : 50
            personHeightCons?.constant = count < 3 ? 76 : 46
        } else {
            personLeadingCons?.constant = 0
            personBottomCons?.constant = 0
            personWidthCons?.constant = itemW
            personHeightCons?.constant = itemH
        }
        model.canvasView = personCanvasView.canvasView
        model.screenCanvasView = screenCanvasView
        UIView.animate(withDuration: 0.25) {
            self.personLeadingCons?.isActive = true
            self.personBottomCons?.isActive = true
            self.personWidthCons?.isActive = true
            self.personHeightCons?.isActive = true
        }
        personCanvasView.setName(name: "\(model.uid)")
        setupCanvasClosure?(model, model.uid == UserInfo.userId)
    }
}


class PersonCanvasView: UIView {
    lazy var canvasView: AGEView = {
        let view = AGEView()
        return view
    }()
    private lazy var nameLabel: AGELabel = {
        let label = AGELabel(colorStyle: .black, fontStyle: .small)
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        layer.cornerRadius = 5
        layer.masksToBounds = true
        addSubview(canvasView)
        addSubview(nameLabel)
        canvasView.translatesAutoresizingMaskIntoConstraints = false
        nameLabel.translatesAutoresizingMaskIntoConstraints = false
        canvasView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        canvasView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        canvasView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        canvasView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        
        nameLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 10).isActive = true
        nameLabel.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -5).isActive = true
        backgroundColor = UIColor().randomColor
    }
    func setName(name: String) {
        nameLabel.text = name
    }
}
