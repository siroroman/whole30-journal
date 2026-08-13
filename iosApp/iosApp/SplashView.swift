import SwiftUI

struct SplashView: View {
    private let backgroundColor = Color(red: 0.06275, green: 0.07059, blue: 0.09020)
    private let accentColor = Color(red: 0.17647, green: 0.83137, blue: 0.74902)
    private let accentOnColor = Color(red: 0.02353, green: 0.14902, blue: 0.12941)
    private let textColor = Color(red: 0.94902, green: 0.95294, blue: 0.96078)
    private let textMutedColor = Color(red: 0.41961, green: 0.43922, blue: 0.47059)

    private let iconSize: CGFloat = 96
    private let markSize: CGFloat = 52

    @State private var isPulsing = false

    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            VStack(spacing: 20) {
                RoundedRectangle(cornerRadius: 26, style: .continuous)
                    .fill(accentColor)
                    .frame(width: iconSize, height: iconSize)
                    .overlay {
                        PlantMarkShape()
                            .stroke(
                                accentOnColor,
                                style: StrokeStyle(lineWidth: 2.2 * markSize / 24, lineCap: .round, lineJoin: .round)
                            )
                            .frame(width: markSize, height: markSize)
                    }

                VStack(spacing: 4) {
                    Text("Whole30")
                        .font(.system(size: 24, weight: .heavy))
                        .tracking(-0.01 * 24)
                        .foregroundStyle(textColor)
                    Text("DIARY")
                        .font(.system(size: 13, weight: .semibold))
                        .tracking(0.04 * 13)
                        .foregroundStyle(textMutedColor)
                }
            }
            Spacer()
            Capsule()
                .fill(accentColor)
                .frame(width: 80, height: 3)
                .opacity(isPulsing ? 1 : 0.3)
                .animation(.easeInOut(duration: 1.4).repeatForever(autoreverses: true), value: isPulsing)
                .padding(.bottom, 14)
                .onAppear { isPulsing = true }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(backgroundColor.ignoresSafeArea())
    }
}

private struct PlantMarkShape: Shape {
    func path(in rect: CGRect) -> Path {
        let scale = rect.width / 24
        func point(_ unitX: CGFloat, _ unitY: CGFloat) -> CGPoint {
            CGPoint(x: rect.minX + unitX * scale, y: rect.minY + unitY * scale)
        }

        var path = Path()

        path.move(to: point(12, 2))
        path.addLine(to: point(12, 8))

        path.move(to: point(8, 4))
        path.addCurve(to: point(12, 10), control1: point(8, 8), control2: point(9, 10))
        path.addCurve(to: point(16, 4), control1: point(15, 10), control2: point(16, 8))

        path.move(to: point(6, 22))
        path.addCurve(to: point(12, 12), control1: point(6, 16), control2: point(8.5, 12))
        path.addCurve(to: point(18, 22), control1: point(15.5, 12), control2: point(18, 16))

        return path
    }
}

#Preview {
    SplashView()
}
